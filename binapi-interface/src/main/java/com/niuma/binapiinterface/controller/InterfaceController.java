package com.niuma.binapiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.niuma.binapiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author niuma
 * @create 2023-03-28 15:33
 */
@RestController
public class InterfaceController {
    @PostMapping("/user")
    public String getUserByPost(@RequestBody User user, HttpServletRequest request){
        return  "用户叫"+user.getUsername();
    }

    @GetMapping("/renjian")
    public String getRenjian(){
        return HttpRequest.get("https://v.api.aa1.cn/api/api-renjian/index.php?type=json").execute().body();
    }
}
