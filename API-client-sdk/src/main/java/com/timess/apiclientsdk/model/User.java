package com.timess.apiclientsdk.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xing10
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 7537376953937883383L;
    /**
     * 用户名称
     */
    private String name;


}
