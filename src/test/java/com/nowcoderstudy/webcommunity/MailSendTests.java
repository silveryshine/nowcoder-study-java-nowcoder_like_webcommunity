package com.nowcoderstudy.webcommunity;
import com.nowcoderstudy.webcommunity.entity.User;
import com.nowcoderstudy.webcommunity.service.UserService;
import com.nowcoderstudy.webcommunity.util.MailUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.expression.ThymeleafEvaluationContext;

@SpringBootTest
@ContextConfiguration(classes=WebcommunityApplication.class)
public class MailSendTests {
    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UserService userService;

    @Test
    public void testMailSend(){
        mailUtil.sendMail("2499876387@qq.com","javamailtest","yeah");
    }

    @Test
    public void testHtmlMailSend(){
        Context context = new Context();
        context.setVariable("username","yahaha");
        String content = templateEngine.process("/mail/maildemo",context);
        mailUtil.sendMail("2499876387@qq.com","javamailtest22",content);

    }

    @Test
    public void testUserRegister(){
        User u = new User();
        u.setPassword("122345");
        u.setEmail("2499876387@qq.com");
        u.setUsername("silveryshine");
        userService.userRegister(u);
    }

}
