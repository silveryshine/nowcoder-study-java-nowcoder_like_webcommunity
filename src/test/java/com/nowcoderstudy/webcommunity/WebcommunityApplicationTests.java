package com.nowcoderstudy.webcommunity;

import com.nowcoderstudy.webcommunity.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.security.RunAs;

@SpringBootTest
class WebcommunityApplicationTests {
    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        System.out.println(userService.findUserById(1).toString());
    }

}
