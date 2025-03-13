package com.timess.manager;

/**
 * @author 33363
 */
import com.timess.apicommon.model.entity.entity.InvokeRecord;
import com.timess.apicommon.service.InnerUserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class AsyncInvokeManager {

    private final ConcurrentLinkedQueue<InvokeRecord> queue = new ConcurrentLinkedQueue<>();
    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    // 每5秒批量处理一次
    @Scheduled(fixedRate = 3000)
    public void processBatch() {
        List<InvokeRecord> batch = new ArrayList<>();
        // 每批100条
        while (!queue.isEmpty() && batch.size() < 100) {
            InvokeRecord record = queue.poll();
            if(record != null) {
                batch.add(record);
            }
        }
        if (!batch.isEmpty()) {
            try {
                innerUserInterfaceInfoService.invokeCount(batch);
                log.debug("批量处理完成: {}条记录", batch.size());
            } catch (Exception e) {
                log.error("批量处理失败，重新入队", e);
                // 失败重试
                queue.addAll(batch);
            }
        }
    }

    // 添加调用记录到队列
    public void addInvokeRecord(Long interfaceId, Long userId) {
        queue.offer(new InvokeRecord(interfaceId, userId));
    }

    // 关闭前处理剩余数据
    @PreDestroy
    public void onShutdown() {
        processBatch();
    }

}
