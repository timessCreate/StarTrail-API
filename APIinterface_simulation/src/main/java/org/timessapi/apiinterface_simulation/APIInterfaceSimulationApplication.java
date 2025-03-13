package org.timessapi.apiinterface_simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author xing10
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class APIInterfaceSimulationApplication {
    public static void main(String[] args) {
        SpringApplication.run(APIInterfaceSimulationApplication.class, args);
    }
}
