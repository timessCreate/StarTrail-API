package com.timess.project.service.impl.inner;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.apicommon.model.entity.entity.InvokeRecord;
import com.timess.apicommon.service.InnerUserInterfaceInfoService;
import com.timess.project.mapper.UserInterfaceInfoMapper;
import com.timess.project.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author xing10
 */
@DubboService
@Slf4j
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invokeCount(List<InvokeRecord> records) {

    }

    @Override
    public Integer leftNumCount(long userId, long interfaceInfoId) {
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("interfaceInfoId",interfaceInfoId);
        UserInterfaceInfo userInterfaceInfo =  userInterfaceInfoService.getOne(queryWrapper);
        return userInterfaceInfo.getLeftNum();
    }
}
