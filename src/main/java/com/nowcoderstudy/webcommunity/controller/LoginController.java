package com.nowcoderstudy.webcommunity.controller;

import com.nowcoderstudy.webcommunity.entity.DiscussPost;
import com.nowcoderstudy.webcommunity.entity.Page;
import com.nowcoderstudy.webcommunity.entity.User;
import com.nowcoderstudy.webcommunity.service.DiscussPostService;
import com.nowcoderstudy.webcommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;


}
