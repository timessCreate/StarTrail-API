package com.timess.apicommon.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.timess.apicommon.model.entity.User;


/**
 * 用户服务
 * @author timess
 */
public interface InnerUserService{

    /**
     * 数据库中查询是否已分配给用户（accessKey）
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);
}
