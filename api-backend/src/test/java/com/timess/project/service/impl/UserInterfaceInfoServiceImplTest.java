package com.timess.project.service.impl;

import com.timess.project.service.UserInterfaceInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class UserInterfaceInfoServiceImplTest {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Test
    void invokeCount() {
        userInterfaceInfoService.invokeCount(1,2);
    }
}