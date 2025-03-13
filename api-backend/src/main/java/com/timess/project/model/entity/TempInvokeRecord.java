package com.timess.project.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TempInvokeRecord {
    private Long userId;
    private Long interfaceInfoId;
    private Integer count;
}