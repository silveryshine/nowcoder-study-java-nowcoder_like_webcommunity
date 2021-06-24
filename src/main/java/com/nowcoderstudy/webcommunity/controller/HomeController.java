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
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path="/test/{id}")
    @ResponseBody
    public String getTest(@PathVariable("id")int id){
        return userService.findUserById(id).toString();
    }

    @RequestMapping(path="index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        List<DiscussPost> postList =  discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(postList!=null){
            for(DiscussPost post : postList){

                User user = userService.findUserById(post.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("user",user);
                map.put("post",post);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);

        page.setRows(discussPostService.findDiscussPostRow(0));
        page.setPath("/index");
        return "/index";
    }
/*
    @RequestMapping(path="/error")
    @ResponseBody
    public String myerror(){
        return "error";
    }*/
}
