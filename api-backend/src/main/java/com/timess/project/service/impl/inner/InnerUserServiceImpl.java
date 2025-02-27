package com.timess.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.timess.apicommon.model.entity.User;
import com.timess.apicommon.service.InnerUserService;
import com.timess.project.common.ErrorCode;
import com.timess.project.exception.BusinessException;
import com.timess.project.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;


/**
 * @author xing10
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 用数据库查询用户是否已经分配对应的 accessKey
     * @param accessKey key
     * @return 查询到的用户信息，如果未查询到返回null
     */
    @Override
    public User getInvokeUser(String accessKey) {
        if(StringUtils.isBlank(accessKey)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //创建条件查询器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey",accessKey);

        //返回查询到的用户
        return userMapper.selectOne(queryWrapper);
    }
}
