package com.timess.project.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 33363
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDeductParam implements Serializable {
    private static final long serialVersionUID = 6354260217387568790L;
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceId;
    /**
     * 累计减扣
     */
    private Integer count;
    /**
     * 版本号
     */
    private Integer version;
    /**
     * 剩余次数
     */
    private Integer currentLeft;

}