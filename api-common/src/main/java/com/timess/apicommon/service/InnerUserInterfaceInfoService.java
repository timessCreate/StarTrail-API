package com.timess.apicommon.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.timess.apicommon.model.entity.InterfaceInfo;
import com.timess.apicommon.model.entity.User;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.apicommon.model.entity.entity.InvokeRecord;

import java.util.List;

/**
* @author xing10
*/
public interface InnerUserInterfaceInfoService{
    /**
     * 批调用统计插入接口
     * @param batch
     */
    void invokeCount(List<InvokeRecord> batch);

    /**
     * 获取用户接口的剩余调用次数
     */
    int leftNumCount(long userId, long interfaceInfoId);

}
