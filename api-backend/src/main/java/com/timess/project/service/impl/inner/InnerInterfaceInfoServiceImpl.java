package com.timess.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.timess.apicommon.model.entity.InterfaceInfo;
import com.timess.apicommon.service.InnerInterfaceInfoService;
import com.timess.project.common.ErrorCode;
import com.timess.project.exception.BusinessException;
import com.timess.project.mapper.InterfaceInfoMapper;
import com.timess.project.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;


/**
 * @author xing10
 */
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;
    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        if(StringUtils.isAnyBlank(path,method)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"path 和 method 均不能为null");
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", path);
        queryWrapper.eq("method", method);

        //使用interfaceInfoMapper 的 SelectOne方法查询接口
        return interfaceInfoMapper.selectOne(queryWrapper);
    }
}
