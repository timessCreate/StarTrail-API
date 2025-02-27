package com.timess.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timess.apicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author xing10
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Mapper
* @createDate 2024-08-22 15:58:56
* @Entity com.yupi.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
        List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




