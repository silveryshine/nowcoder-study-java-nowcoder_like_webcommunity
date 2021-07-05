package com.nowcoderstudy.webcommunity.service;

import com.nowcoderstudy.webcommunity.dao.LoginTicketMapper;
import com.nowcoderstudy.webcommunity.entity.LoginTicket;
import com.nowcoderstudy.webcommunity.util.CommunityUtil;
import com.nowcoderstudy.webcommunity.util.MailUtil;
import com.nowcoderstudy.webcommunity.util.WebcommunityConstantUtil;
import org.apache.commons.lang3.StringUtils;
import com.nowcoderstudy.webcommunity.dao.UserMapper;
import com.nowcoderstudy.webcommunity.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements WebcommunityConstantUtil {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailUtil mailUtil;

    @Value("${webcommunity.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public User findUserByName(String name){
        return  userMapper.selectByName(name);
    }

    public Map<String,Object> userRegister(User user){
        Map<String, Object> map = new HashMap<>();
        if(user==null){
            throw new IllegalArgumentException("null argument!");
        }

        if(StringUtils.isBlank(user.getUsername())){
            map.put("username","null username!");
        }

        if(StringUtils.isBlank(user.getEmail())){
            map.put("email","null email!");
        }

        if(StringUtils.isBlank(user.getPassword())){
            map.put("password","null password!");
        }

        if(!map.isEmpty()){
            return map;
        }


        User forCheck = userMapper.selectByName(user.getUsername());
        if(forCheck!=null){
            map.put("username","username already used");
            return map;
        }

        forCheck = userMapper.selectByEmail(user.getEmail());
        if(forCheck!=null){
            map.put("email","email already used");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setCreateTime(new Date());
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));;
        user.setStatus(0);
        user.setType(0);
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        userMapper.insertUser(user);

        //send email
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        context.setVariable("url",domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode());
        String content = templateEngine.process("/mail/activation",context);
        mailUtil.sendMail(user.getEmail(),"account activation",content);
        return map;


    }

    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }
        if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FIAL;
    }

    public Map<String,Object> login(String name,String password,long expiredSeconds){
        Map<String, Object> map=new HashMap<>();
        if(StringUtils.isBlank(name)){
            map.put("usernameMsg","null name");
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","null password");
        }
        if(!map.isEmpty()){
            return map;
        }
        User user = userMapper.selectByName(name);
        if(user==null){
            map.put("usernameMsg","null user");
            return map;
        }

        if(user.getStatus()==0){
            map.put("usernameMsg","account not activated");
            return map;
        }

        String md5edPassword = CommunityUtil.md5(password+user.getSalt());
        if(!md5edPassword.equals(user.getPassword())){
            map.put("passwordMsg","wrong password");
            return map;
        }

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public int logout(String loginTicket){
        return loginTicketMapper.updateStatus(loginTicket,1);
    }

    public LoginTicket getLoginTicketByTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    public LoginTicket[] getLoginTicketsByUserId(int userId){
        return loginTicketMapper.selectByUserId(userId);
    }

    public int deleteLoginTicketByTicket(String ticket){
        return loginTicketMapper.deleteByTicket(ticket);
    }

    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

    public Map<String, Object> updatePassword(int userId,String oldPassword,String newPassword){
        Map<String,Object> map = new HashMap<>();
        User user = userMapper.selectById(userId);
        if(StringUtils.isBlank(oldPassword)||StringUtils.isBlank(newPassword)){
            map.put("passwordMsg","null password");
            return map;
        }
        oldPassword=CommunityUtil.md5(oldPassword+user.getSalt());
        if(!oldPassword.equals(user.getPassword())){
            map.put("passswordMsg","incorrect old password");
            return map;
        }
        newPassword=CommunityUtil.md5(newPassword+user.getSalt());
        userMapper.updatePassword(userId,newPassword);
        return map;
    }
}
