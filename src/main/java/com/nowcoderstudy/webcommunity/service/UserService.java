package com.nowcoderstudy.webcommunity.service;

import com.nowcoderstudy.webcommunity.dao.UserMapper;
import com.nowcoderstudy.webcommunity.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
