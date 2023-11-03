package com.niuma.binapi.service;

import com.niuma.binapicommon.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
 * 用户服务测试
 *
 * @author niuma
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;


    @Test
    void userRegister() {
        // 注册一个用户，无需手机验证码
        String password = DigestUtils.md5DigestAsHex(("dogbin" + "12345678").getBytes());
        User user = new User();
        user.setUserName("dogbin");
        user.setUserAccount("dogbin");
        user.setGender(0);
        user.setUserRole("admin");
        user.setUserPassword(password);
        user.setPhoneNum("13623123123");
        user.setAccessKey("aaaaa");
        user.setSecretKey("bbbbb");
        boolean save = userService.save(user);
        System.out.println(save);
    }

    @Test
    void testAddUser() {
        User user = new User();
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        boolean result = userService.updateById(user);
        Assertions.assertTrue(result);
    }

    @Test
    void testDeleteUser() {
        boolean result = userService.removeById(1L);
        Assertions.assertTrue(result);
    }

    @Test
    void testGetUser() {
        User user = userService.getById(1L);
        Assertions.assertNotNull(user);
    }


}