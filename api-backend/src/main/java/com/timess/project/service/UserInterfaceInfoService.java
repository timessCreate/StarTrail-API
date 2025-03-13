package com.timess.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.timess.apicommon.model.entity.UserInterfaceInfo;

/**
 * @author xing10
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {


    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    void invokeCount(long interfaceInfoId, long userId);
}
