package com.timess.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.project.common.ErrorCode;
import com.timess.project.exception.BusinessException;
import com.timess.project.mapper.UserInterfaceInfoMapper;
import com.timess.project.service.UserInterfaceInfoService;
import org.springframework.stereotype.Service;

/**
* @author xing10
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Service实现
* @createDate 2024-08-22 15:58:56
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if(userInterfaceInfo == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //创建时所有参数必须为非空
        if(add){
            if(userInterfaceInfo.getInterfaceInfoId() <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"名称过长");
            }
        }

    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if(interfaceInfoId <= 0 || userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper= new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.gt("leftNum",0);
        updateWrapper.setSql("leftNum = leftNum - 1, totalNum = totalNum + 1");
        this.update(updateWrapper);
        return true;
    }
}




