package com.timess.apiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.timess.apiclientsdk.model.User;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import static com.timess.apiclientsdk.utils.SignUtils.genSign;


/**
 * @author xing10
 * //模拟前端向后端发送请求
 * 调用第三方接口的客户端
 */
@Slf4j
public class APIClient {

    private String accessKey;
    private String secretKey;

    private final String GATEWAY_HOST = "http://localhost:8090";
    public APIClient() {
    }

    public APIClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    private Map<String, String> getHeaderMap(String body){
        Map<String,String>  hashMap = new HashMap<>();
        hashMap.put("accessKey", accessKey);
        //不允许直接发送密钥,通过签名的形式
        //hashMap.put("secretKey", secretKey);
        hashMap.put("sign", genSign(body, secretKey));
        hashMap.put("nonce", RandomUtil.randomNumbers(5));
        hashMap.put("body",body);
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        return hashMap;
    }
     /**
     * 以对象形式请求接口
     * @param user 用户类
     * @return 用户名称
     */
    public String getUserNameByPost(User user){
        String json = JSONUtil.toJsonStr(user);
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/name/user")
                .addHeaders(getHeaderMap(json))
                .body(json)
                .execute();
        System.out.println("getUserNameByPost的响应码为：" + httpResponse.getStatus());
        String result = httpResponse.body();
        System.out.println(result);
        return result;
    }
}
