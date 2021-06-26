package com.nowcoderstudy.webcommunity.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoderstudy.webcommunity.entity.DiscussPost;
import com.nowcoderstudy.webcommunity.entity.Page;
import com.nowcoderstudy.webcommunity.entity.User;
import com.nowcoderstudy.webcommunity.service.DiscussPostService;
import com.nowcoderstudy.webcommunity.service.UserService;
import com.nowcoderstudy.webcommunity.util.WebcommunityConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController implements WebcommunityConstantUtil {
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
}
