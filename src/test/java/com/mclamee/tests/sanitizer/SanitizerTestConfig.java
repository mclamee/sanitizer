package com.mclamee.tests.sanitizer;

import java.util.List;

import com.mclamee.EnableSanitizerModule;
import com.mclamee.sanitizer.Sanitizer;
import org.springframework.context.annotation.Bean;

@EnableSanitizerModule
public class SanitizerTestConfig {

    @Bean
    public TestControllerBean getController() {
        return new TestControllerBean();
    }

    // cross class test: package accessibility
    @Sanitizer("anyName")
    private static List<TestUserVo> sanitizeUserListMethodName(List<TestUserVo> userVos) {
        if (userVos != null) {
            userVos.stream().forEach(i -> {
                if (i.getUserName() != null) {
                    i.setUserName("byMethod");
                }
            });
        }
        return userVos;
    }

    // cross class test: lower priority
    @Sanitizer("loopCall")
    public static TestUserVo loopCall(TestUserVo userVo) {
        userVo.setUserName("Inside Config Class");
        return userVo;
    }
}