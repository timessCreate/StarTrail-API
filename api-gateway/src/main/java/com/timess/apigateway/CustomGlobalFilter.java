package com.timess.apigateway;

import com.timess.apiclientsdk.utils.SignUtils;
import com.timess.apicommon.model.entity.InterfaceInfo;
import com.timess.apicommon.model.entity.User;
import com.timess.apicommon.service.InnerInterfaceInfoService;
import com.timess.apicommon.service.InnerUserInterfaceInfoService;
import com.timess.apicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * @author xing10
 * 全局过滤器
 * 在此处完成业务逻辑校验
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    private String INTERFACE_HOST = "http://localhost:8123";
    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        1. 记录日志
        ServerHttpRequest request = exchange.getRequest();
        log.info("=========================== ");
        log.info("请求唯一标识： " + request.getId());

        String path = INTERFACE_HOST + request.getPath().value();
        log.info("请求路径： " + path);

        String method = Objects.requireNonNull(request.getMethod()).toString();
        log.info("请求方法： " + method);

        String params = request.getQueryParams().toString();
        log.info("请求参数： " + params);

        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求本地地址： " + sourceAddress);
        log.info("请求来源地址： " + request.getRemoteAddress());
        log.info("=========================== ");
        ServerHttpResponse response = exchange.getResponse();

//        2. 校验该用户是否在黑白名单内
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            return this.handleNoAuth(response);
        }
//        3. 用户鉴权(判断 ak，sk 是否合法)
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        String timestamp = headers.getFirst("timestamp");

        //去数据库中查询accessKey 是否已分配给用户
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        }catch (Exception e){
            //为查询到该用户，记录日志
            log.info("get invokeUser error", e);
        }
        if(invokeUser == null){
            //用户信息为null, 处理未授权情况，并返回响应
            return handleNoAuth(response);
        }

        //nonce随机数长度判定
        assert nonce != null;
        if (Long.parseLong(nonce) > 100000L) {
            return this.handleNoAuth(response);
        }
        //timestamp时间戳：
        //时间和当前时间不超过五分钟
        Long currentTime = System.currentTimeMillis() / 1000;
        final Long FIVE_MINUTES = 60 * 5L;
        if (currentTime - Long.parseLong(timestamp) > FIVE_MINUTES) {
            return this.handleNoAuth(response);
        }

        //从数据库中查询secretKey
        //从刚才获取的invokeUser对象中获取该信息
        String secretKey = invokeUser.getSecretKey();
        //使用获取到的密钥对请求体进行签名
        String serverSign = SignUtils.genSign(body,secretKey);
        if(sign == null || !sign.equals(serverSign)){
            //如果签名为null或者不一致，返回处理未授权的响应
            return handleNoAuth(response);
        }

//       4. 请求的模拟接口是否存在？ 以及请求方法是否匹配
        //初始化一个空的接口对象存储查询到的结果
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path,method);
        } catch (Exception e) {
            //接口不存在，记录日志
            log.info("getInterfaceInfo error", e);
        }
        if(interfaceInfo == null){
            return handleNoAuth(response);
        }

//        5. 校验请求参数是否合法
        //校验接口接口调用次数是否 > 0
        int leftNumCount = innerUserInterfaceInfoService.leftNumCount(interfaceInfo.getId(), invokeUser.getId());
        if(leftNumCount == 0){
            return handleNoAuth(response);
        }
////        6. 请求转发，调用模拟接口
//        Mono<Void> filter = chain.filter(exchange);

//        7. 响应日志 + 调用次数 + 1
        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());

    }

    /**
     * 装饰器添加日志
     *
     * @param exchange
     * @param chain
     * @param
     * @param
     * @return
     */

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7. 调用成功，接口调用次数 + 1 invokeCount
                                        try {
                                            innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        //释放掉内存
                                        DataBufferUtils.release(dataBuffer);
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            // 降级处理返回数据
            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }

    /**
     * 无权限，禁止访问
     *
     * @param response
     * @return
     */
    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    /**
     * 调用失败返回
     *
     * @param response
     * @return
     */
    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}