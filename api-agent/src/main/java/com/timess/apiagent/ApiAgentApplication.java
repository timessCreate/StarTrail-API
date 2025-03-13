package com.timess.apiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiAgentApplication.class, args);
    }

}
