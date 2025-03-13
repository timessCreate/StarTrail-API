package com.timess.apiagent.controller;


import com.timess.apiclientsdk.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.timess.apiclientsdk.client.APIClient;
import javax.annotation.Resource;

@RestController
public class AgentController {

    @Resource
    APIClient apiClient;
    @GetMapping("/test")
    public String result(){
        User user = new User();
        user.setName("test");
        String userNameByPost = apiClient.getUserNameByPost(user);
        return userNameByPost;
    }
}
