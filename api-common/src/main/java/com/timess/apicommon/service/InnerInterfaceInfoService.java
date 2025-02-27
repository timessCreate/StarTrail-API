package com.timess.apicommon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.timess.apicommon.model.entity.InterfaceInfo;


/**
* @author xing10
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-08-16 11:52:23
*/
public interface InnerInterfaceInfoService{
    /**
     * 从数据库中查找模拟接口是否存在
     * @param path 接口路径
     * @param method 方法
     * @return 模拟接口信息
     */
    InterfaceInfo getInterfaceInfo(String path, String method);
}
