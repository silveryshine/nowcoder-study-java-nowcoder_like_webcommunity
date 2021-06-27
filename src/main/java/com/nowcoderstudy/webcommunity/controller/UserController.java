package com.nowcoderstudy.webcommunity.controller;

import com.nowcoderstudy.webcommunity.annotation.LoginRequiredAnnotation;
import com.nowcoderstudy.webcommunity.entity.User;
import com.nowcoderstudy.webcommunity.service.UserService;
import com.nowcoderstudy.webcommunity.util.CommunityUtil;
import com.nowcoderstudy.webcommunity.util.HostHolder;
import org.apache.catalina.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping(path = "/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${webcommunity.path.upload}")
    private String uploadPath;

    @Value("${webcommunity.path.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequiredAnnotation
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequiredAnnotation
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(Model model, MultipartFile headerImage){
        if(headerImage==null){
            model.addAttribute("error","header file not selected");
            return "/site/setting";
        }

        String originNmae = headerImage.getOriginalFilename();
        String type = originNmae.substring(originNmae.lastIndexOf('.')+1);
        if(type==null){
            model.addAttribute("error","worng file type");
            return "/site/setting";
        }
        originNmae=CommunityUtil.generateUUID()+"."+type;
        File destination = new File(uploadPath+"/"+originNmae);
        try{
            headerImage.transferTo(destination);
        }
        catch (IOException e){
            logger.error("upload failed "+e.getMessage());
            throw new RuntimeException("upload failed");
        }

        User user= hostHolder.getUser();
        userService.updateHeader(user.getId(),domain+contextPath+"/user/header/"+originNmae);
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeaderImage(@PathVariable("fileName")String fileName, HttpServletRequest request, HttpServletResponse response){
        fileName=uploadPath+"/"+fileName;
        String type = fileName.substring(fileName.lastIndexOf('.')+1);
        response.setContentType("image/"+type);
        try{
            OutputStream ops = response.getOutputStream();
            FileInputStream fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int bbyte = 0;
            while((bbyte=fis.read(buffer))!=-1){
                ops.write(buffer,0,bbyte);
            }
            fis.close();
        }
        catch (IOException e){
            logger.error("read failed "+e.getMessage());
        }
    }

    @RequestMapping(path = "/password",method = RequestMethod.POST)
    public String changePassword(Model model,String newpassword,String oldpassword){
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user.getId(),oldpassword,newpassword);
        if(!map.isEmpty()){
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
        }
        return "/site/setting";
    }
}
