package com.timess.apicommon.model.entity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvokeRecord implements Serializable {
    private static final long serialVersionUID = 2626097172458541825L;
    /**
     * 接口id
     */
    private long interfaceInfoId;

    /**
     * 用户id
     */
    private long userId;

}
