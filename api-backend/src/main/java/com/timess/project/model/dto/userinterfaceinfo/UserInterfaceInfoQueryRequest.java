package com.timess.project.model.dto.userinterfaceinfo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.timess.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author timess
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserInterfaceInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * 主键
     */
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


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}