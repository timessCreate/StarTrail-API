package org.timessapi.apiinterface_simulation;

import com.timess.apiclientsdk.client.APIClient;
import com.timess.apiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ApIInterfaceSimulationApplicationTests {
    @Resource
    private APIClient apiClient;

    @Test
    void contextLoads() {
        String result = apiClient.getNameByGet("timess");
        User user = new User();
        user.setName("dbtimess");
        String userNameByPost = apiClient.getUserNameByPost(user);
        System.out.println(userNameByPost);

    }

}
