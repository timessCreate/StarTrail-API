package com.timess.apiclientsdk;

import com.timess.apiclientsdk.client.APIClient;
import com.timess.apiclientsdk.model.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author xing10
 */
@Configuration
@ConfigurationProperties("api.client")
@Data
@ComponentScan
public class APIClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public APIClient apiClient(){
       return new APIClient(accessKey,secretKey);
    }

}
