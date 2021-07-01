package com.nowcoderstudy.webcommunity;

import com.nowcoderstudy.webcommunity.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes=WebcommunityApplication.class)
public class StringTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerTests.class);

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testStringReplaceSensitive(){
        String out =  sensitiveFilter.replaceSensitive("赌博");
        System.out.println(out);
    }
}
