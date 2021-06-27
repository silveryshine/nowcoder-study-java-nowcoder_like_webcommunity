package com.nowcoderstudy.webcommunity.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoderstudy.webcommunity.entity.DiscussPost;
import com.nowcoderstudy.webcommunity.entity.LoginTicket;
import com.nowcoderstudy.webcommunity.entity.Page;
import com.nowcoderstudy.webcommunity.entity.User;
import com.nowcoderstudy.webcommunity.service.DiscussPostService;
import com.nowcoderstudy.webcommunity.service.UserService;
import com.nowcoderstudy.webcommunity.util.WebcommunityConstantUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController implements WebcommunityConstantUtil {
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path="/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path="register",method = RequestMethod.POST)
    public String register(Model model,User user){
        Map<String, Object>map=userService.userRegister(user);
        if(map.isEmpty()){
            model.addAttribute("msg","register succeed, activation email has been sent");
            model.addAttribute("target","/index");
            return  "/site/operate-result";
        }
        else {
            model.addAttribute("usernameMsg", map.get("username"));
            model.addAttribute("passwordMsg", map.get("password"));
            model.addAttribute("emailMsg", map.get("email"));
            return "/site/register";
        }
    }

    @RequestMapping(path="/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model,@PathVariable("userId")int userId,@PathVariable("code")String code){
        int result = userService.activation(userId,code);
        if(result==ACTIVATION_SUCCESS){
            model.addAttribute("msg", "Success. Account is now activated");
            model.addAttribute("target", "/login");
        }
        else if(result==ACTIVATION_REPEAT){
            model.addAttribute("msg", "Account is already activated");
            model.addAttribute("target", "/index");
        }
        else{
            model.addAttribute("msg", "Activation failed");
            model.addAttribute("target", "/login");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }


    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        session.setAttribute("kaptcha",text);
        response.setContentType("image/png");
        try{
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        }
        catch (IOException e){
            logger.error("kaptcha failed "+e.getMessage());
        }
    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code, boolean remember, HttpSession session, HttpServletResponse response){
        String kaptcha = (String)session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "invalid code!");
            return "/site/login";
        }
        long expiredTime = remember?DEFAULT_EXPIRE_TIME_SENCOND:REMEMBER_EXPIRE_TIME_SEOND;
        Map<String,Object> map = userService.login(username,password,expiredTime);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge((int)expiredTime);
            response.addCookie(cookie);
            return "redirect:/index";
        }
        else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        return "redirect:/index";
    }
}
