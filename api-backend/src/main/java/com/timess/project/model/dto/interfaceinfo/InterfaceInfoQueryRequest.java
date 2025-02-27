package com.timess.project.model.dto.interfaceinfo;

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
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口状态
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人id
     */
    private Integer userId;

    /**
     * 请求参数
     */
    private String requestParams;


    private static final long serialVersionUID = 1L;


}