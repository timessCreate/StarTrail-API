package com.timess.project.service;

import com.timess.apicommon.model.entity.InterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author xing10
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-08-16 11:52:23
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
