package com.timess.project.service.impl.inner;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.apicommon.service.InnerUserInterfaceInfoService;
import com.timess.project.service.UserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;


/**
 * @author xing10
 */
@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        //注入userInterfaceInfoService的接口调用计数方法
        return userInterfaceInfoService.invokeCount(interfaceInfoId,userId);
    }

    @Override
    public int leftNumCount(long interfaceInfoId, long userId) {
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceInfoId",interfaceInfoId);
        queryWrapper.eq("userId", userId);
        UserInterfaceInfo userInterfaceInfo =  userInterfaceInfoService.getOne(queryWrapper);
        return userInterfaceInfo.getLeftNum();
    }
}
