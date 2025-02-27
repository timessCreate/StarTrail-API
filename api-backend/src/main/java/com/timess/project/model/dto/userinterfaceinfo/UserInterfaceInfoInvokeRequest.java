package com.timess.project.model.dto.userinterfaceinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xing10
 * 接口调用请求
 */
@Data
public class UserInterfaceInfoInvokeRequest implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 0-正常， 1-禁用
     */
    private Integer status;

    /**
     * 是否删除(0-未删除, 1-已删除)
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
