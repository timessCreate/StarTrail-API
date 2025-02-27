package com.timess.project.model.dto.userinterfaceinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建请求
 *
 * @author xing10
 * @TableName product
 */
@Data
public class UserInterfaceInfoAddRequest implements Serializable {
    /**
     * 调用者id
     */
    private Long userId;

    /**
     * 被调用接口id
     */
    private Long interfaceInfoId;

    /**
     * 总调用次数
     */
    private Integer totalNum;

    /**
     * 该接口剩余调用次数
     */
    private Integer leftNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}