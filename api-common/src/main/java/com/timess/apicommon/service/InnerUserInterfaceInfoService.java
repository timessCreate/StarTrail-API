package com.timess.apicommon.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.timess.apicommon.model.entity.InterfaceInfo;
import com.timess.apicommon.model.entity.User;
import com.timess.apicommon.model.entity.UserInterfaceInfo;

/**
* @author xing10
*/
public interface InnerUserInterfaceInfoService{
    /**
     * 调用接口统计
     * @param interfaceInfoId  接口id
     * @param userId 用户id
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 获取用户接口的剩余调用次数
     */
    int leftNumCount(long interfaceInfoId, long userId);

}
