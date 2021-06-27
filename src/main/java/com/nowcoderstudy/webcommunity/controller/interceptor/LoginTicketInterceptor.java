package com.nowcoderstudy.webcommunity.controller.interceptor;

import com.nowcoderstudy.webcommunity.entity.LoginTicket;
import com.nowcoderstudy.webcommunity.entity.User;
import com.nowcoderstudy.webcommunity.service.UserService;
import com.nowcoderstudy.webcommunity.util.CookieUtil;
import com.nowcoderstudy.webcommunity.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    private static final Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ticket = CookieUtil.getCookie(request, "ticket");
        if (ticket == null) {
            return true;
        }
        LoginTicket loginTicket = userService.getLoginTicketByTicket(ticket);
        if (loginTicket == null) {
            return true;
        }
        if (loginTicket.getStatus() == 1 || !loginTicket.getExpired().after(new Date())) {
            userService.deleteLoginTicketByTicket(ticket);
            return true;
        }
        int userId = loginTicket.getUserId();
        User user = userService.findUserById(userId);
        hostHolder.setUser(user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null&&modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        hostHolder.clear();
    }
}
