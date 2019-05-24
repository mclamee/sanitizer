package com.mclamee.sanitizer;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAspectJAutoProxy
@Import( {SanitizerAspect.class, SanitizerPostProcessor.class, SanitizerCache.class})
public class SanitizerTestConfig {

    @Bean
    public TestControllerBean getController() {
        return new TestControllerBean();
    }

    // cross class test: package accessibility
    @Sanitizer("anyName")
    private static List<UserVo> sanitizeUserListMethodName(List<UserVo> userVos) {
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
    public static UserVo loopCall(UserVo userVo) {
        userVo.setUserName("Inside Config Class");
        return userVo;
    }
}