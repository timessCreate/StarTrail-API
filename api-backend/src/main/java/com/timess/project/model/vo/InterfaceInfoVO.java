package com.timess.project.model.vo;

import com.timess.project.model.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子视图
 *
 * @author timess
 * @TableName product
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoVO extends Post {


    /**
     * 调用次数
     */
    private int totalNum;

    private static final long serialVersionUID = 1L;
}