package com.niuma.binapiinterface.controller;

import com.niuma.binapiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * @author niumazlb
 * @create 2022-11-09 10:55
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @PostMapping("/user")
    public String getUserByPost(@RequestBody User user, HttpServletRequest request){
        return  "Post用户叫"+user.getUsername();
    }
}
