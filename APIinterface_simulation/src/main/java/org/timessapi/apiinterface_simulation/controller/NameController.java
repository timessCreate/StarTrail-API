package org.timessapi.apiinterface_simulation.controller;


import com.timess.apiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * @author xing10
 * 名称api
 */

@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping("/test")
    public String getNameByGet(HttpServletRequest request) {
        String name  = request.getHeader("timess");
        System.out.println("Get + 你的名字是" + name);
        return "Get + 你的名字是" + name;
    }

    @GetMapping("/get")
    public String getNameByGet(@RequestParam String name) {
        System.out.println("Get + 你的名字是" + name);
        return "Get + 你的名字是" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "Post + 你的名字是" + name;
    }


    /**
     * 以对象形式请求接口
     * @param user 仅包含id的用户类
     * @return 用户名称
     */
    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user) {
        String result = "调用getUserNameByPost接口： " + "用户名字是 ：" + user.getName();
        return result;
    }
}