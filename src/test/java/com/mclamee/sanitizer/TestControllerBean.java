package com.mclamee.sanitizer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

public class TestControllerBean {

    public List<UserVo> callDefaultSanitizer(@Sanitized List<UserVo> userVos) {
        return userVos;
    }

    public List<UserVo> callSanitizerByMethodName(@Sanitized("sanitizeUserListMethodName") List<UserVo> userVos) {
        return userVos;
    }

    @Data
    @AllArgsConstructor
    public static class Result {
        private List<UserVo> userVos;
        private UserVo userVo;

    }

    public Result callSpecialSanitizer(@Sanitized("specialName") List<UserVo> userVos, @Sanitized UserVo userVo) {
        return new Result(userVos, userVo);
    }

    public Result callInvalidSanitizer(@Sanitized("loopCall") List<UserVo> userVos, @Sanitized UserVo userVo) {
        return new Result(userVos, userVo);
    }

    public void callNonExistsSanitizer(@Sanitized("specialName") TestControllerBean bean) {
    }

    @Sanitizer
    public static List<UserVo> sanitizeUserListDefault(List<UserVo> userVos) {
        if (userVos != null) {
            userVos.stream().forEach(i -> {
                if (i.getUserName() != null) {
                    i.setUserName("default");
                }
            });
        }
        return userVos;
    }

    @Sanitizer("specialName")
    public static List<UserVo> sanitizeUserListSpecial(List<UserVo> userVos) {
        if (userVos != null) {
            userVos.stream().forEach(i -> {
                if (i.getUserName() != null) {
                    i.setUserName(StringUtils.trim(i.getUserName().replaceAll("test", "")));
                }
            });
        }
        return userVos;
    }

    @Sanitizer
    public static UserVo sanitizeVo(UserVo userVo) {
        userVo.setUserName("changed");
        return userVo;
    }

    @Sanitizer("loopCall")
    public static UserVo loopCall(@Sanitized("specialName") UserVo userVo) {
        // no change and return directly
        return userVo;
    }
}