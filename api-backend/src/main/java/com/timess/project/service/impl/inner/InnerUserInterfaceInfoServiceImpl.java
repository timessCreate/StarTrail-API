package com.timess.project.service.impl.inner;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.apicommon.model.entity.entity.InvokeRecord;
import com.timess.apicommon.service.InnerUserInterfaceInfoService;
import com.timess.project.common.ErrorCode;
import com.timess.project.exception.BusinessException;
import com.timess.project.mapper.UserInterfaceInfoMapper;
import com.timess.project.model.entity.TempInvokeRecord;
import com.timess.project.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author xing10
 */
@DubboService
@Slf4j
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invokeCount(List<InvokeRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 1. 统计每个(userId, interfaceId)的调用次数
        Map<String, Integer> countMap = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getUserId() + ":" + r.getInterfaceInfoId(),
                        Collectors.summingInt(r -> 1)
                ));

        // 2. 创建临时表并批量插入统计结果
        createAndFillTempTable(countMap);

        // 3. 执行单条高效更新SQL
        int affected = userInterfaceInfoMapper.batchDeductByTempTable();

        // 4. 处理失败记录
        if (affected < countMap.size()) {
            List<InvokeRecord> failed = findFailedRecords(countMap);
            log.warn("扣减失败记录: {}", failed);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "部分次数不足");
        }
    }

    // 创建临时表并插入数据
    private void createAndFillTempTable(Map<String, Integer> countMap) {
        // 删除旧临时表（如果存在）
        userInterfaceInfoMapper.dropTempTableIfExists();
        // 创建临时表
        userInterfaceInfoMapper.createTempTable();

        // 批量插入统计结果
        List<TempInvokeRecord> tempRecords = countMap.entrySet().stream()
                .map(entry -> {
                    String[] keys = entry.getKey().split(":");
                    return new TempInvokeRecord(
                            Long.parseLong(keys[0]),
                            Long.parseLong(keys[1]),
                            entry.getValue()
                    );
                })
                .collect(Collectors.toList());
        userInterfaceInfoMapper.batchInsert(tempRecords);
    }

    // 查找失败记录
    private List<InvokeRecord> findFailedRecords(Map<String, Integer> countMap) {
        return countMap.entrySet().stream()
                .filter(entry -> {
                    String[] keys = entry.getKey().split(":");
                    Long userId = Long.parseLong(keys[0]);
                    Long interfaceId = Long.parseLong(keys[1]);
                    Integer current = userInterfaceInfoMapper.selectLeftNum(interfaceId, userId);
                    return current == null || current < entry.getValue();
                })
                .map(entry -> {
                    String[] keys = entry.getKey().split(":");
                    return new InvokeRecord(
                            Long.parseLong(keys[0]),
                            Long.parseLong(keys[1])
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public int leftNumCount(long userId, long interfaceInfoId) {
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceInfoId",interfaceInfoId);
        queryWrapper.eq("userId", userId);
        UserInterfaceInfo userInterfaceInfo =  userInterfaceInfoService.getOne(queryWrapper);
        return userInterfaceInfo.getLeftNum();
    }
}
