package com.timess.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.project.model.entity.TempInvokeRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author xing10
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Mapper
* @createDate 2024-08-22 15:58:56
* @Entity com.yupi.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
        List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);


        //创建临时表
        void createTempTable();

        //批量插入临时表
        void batchInsert(@Param("list")List<TempInvokeRecord> records);

        //批量减扣次数
        int batchDeductByTempTable();

        // 查询剩余次数
        Integer selectLeftNum(
                @Param("userId") Long userId,
                @Param("interfaceId") Long interfaceId
        );

        @Update("DROP TABLE IF EXISTS temp_invoke_records")
        void dropTempTableIfExists();
}




