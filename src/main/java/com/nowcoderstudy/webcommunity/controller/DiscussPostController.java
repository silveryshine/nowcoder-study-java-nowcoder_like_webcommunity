package com.nowcoderstudy.webcommunity.controller;

import com.nowcoderstudy.webcommunity.entity.Comment;
import com.nowcoderstudy.webcommunity.entity.DiscussPost;
import com.nowcoderstudy.webcommunity.entity.Page;
import com.nowcoderstudy.webcommunity.entity.User;
import com.nowcoderstudy.webcommunity.service.CommentService;
import com.nowcoderstudy.webcommunity.service.DiscussPostService;
import com.nowcoderstudy.webcommunity.service.LikeService;
import com.nowcoderstudy.webcommunity.service.UserService;
import com.nowcoderstudy.webcommunity.util.CommunityConstant;
import com.nowcoderstudy.webcommunity.util.CommunityUtil;
import com.nowcoderstudy.webcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@RequestMapping(path = "/discuss")
@Controller
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = holder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"not login");
        }


        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPost.setUserId(user.getId());
        discussPostService.addDiscussPost(discussPost);
        return CommunityUtil.getJSONString(0,"post success");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // ??????
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // ??????
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);
        int likeStatus = likeService.findLikeStatus(user.getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        // ??????????????????
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // ??????: ??????????????????
        // ??????: ??????????????????
        // ????????????
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // ??????VO??????
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // ??????VO
                Map<String, Object> commentVo = new HashMap<>();
                // ??????
                commentVo.put("comment", comment);
                // ??????
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                likeStatus = likeService.findLikeStatus(user.getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);

                // ????????????
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // ??????VO??????
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // ??????
                        replyVo.put("reply", reply);
                        // ??????
                        replyVo.put("user", userService.findUserById(reply.getUserId()));

                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        likeStatus = likeService.findLikeStatus(user.getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        // ????????????
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // ????????????
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }
}
