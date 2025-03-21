package com.timess.project.model.dto.userinterfaceinfo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;

/**
 * 更新请求
 *
 * @author xing10
 * @TableName product
 */

@Data
public class UserInterfaceInfoUpdateRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;

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