package com.timess.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.project.model.entity.BatchDeductParam;
import com.timess.project.rabbitmq.BatchConsumer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author xing10
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Mapper
* @createDate 2024-08-22 15:58:56
* @Entity com.yupi.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    /**
     * 批量扣减接口调用次数（带版本校验）
     * @param params 扣减参数列表
     * @return 实际成功更新的记录数
     */
    int batchDeductWithVersion(List<BatchDeductParam> params);

    /**
     * 批量查询用户接口信息（根据组合键）
     * @param keys 组合键列表 (userId + interfaceId)
     * @return 用户接口信息列表
     */
    List<UserInterfaceInfo> batchSelectByCompositeKeys(
            @Param("keys") List<BatchConsumer.CompositeKey> keys
    );
}




