package com.nowcoderstudy.webcommunity.service;

import com.nowcoderstudy.webcommunity.dao.DiscussPostMapper;
import com.nowcoderstudy.webcommunity.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

    public int findDiscussPostRow(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

}
