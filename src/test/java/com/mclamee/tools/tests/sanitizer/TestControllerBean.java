package com.mclamee.tools.tests.sanitizer;

import java.util.List;

import com.mclamee.tools.sanitizer.Sanitized;
import com.mclamee.tools.sanitizer.Sanitizer;
import com.mclamee.tools.sanitizer.util.WhiteSpaceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class TestControllerBean {

    public List<TestUserVo> callDefaultSanitizer(@Sanitized List<TestUserVo> userVos) {
        return userVos;
    }

    public List<TestUserVo> callSanitizerByMethodName(@Sanitized("sanitizeUserListMethodName") List<TestUserVo> userVos) {
        return userVos;
    }

    @Data
    @AllArgsConstructor
    public static class Result {
        private List<TestUserVo> userVos;
        private TestUserVo userVo;

    }

    public Result callSpecialSanitizer(@Sanitized("specialName") List<TestUserVo> userVos, @Sanitized TestUserVo userVo) {
        return new Result(userVos, userVo);
    }

    public Result callInvalidSanitizer(@Sanitized("loopCall") List<TestUserVo> userVos, @Sanitized TestUserVo userVo) {
        return new Result(userVos, userVo);
    }

    public void callNonExistsSanitizer(@Sanitized("specialName") TestControllerBean bean) {
    }

    @Sanitizer
    public static List<TestUserVo> sanitizeUserListDefault(List<TestUserVo> userVos) {
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
    public static List<TestUserVo> sanitizeUserListSpecial(List<TestUserVo> userVos) {
        if (userVos != null) {
            userVos.stream().forEach(i -> {

                if (i.getUserName() != null) {
                    i.setUserName(WhiteSpaceUtil.sanitize(i.getUserName().replaceAll("test", "")));
                }
            });
        }
        return userVos;
    }

    @Sanitizer
    public static TestUserVo sanitizeVo(TestUserVo userVo) {
        userVo.setUserName("changed");
        return userVo;
    }

    @Sanitizer("loopCall")
    public static TestUserVo loopCall(@Sanitized("specialName") TestUserVo userVo) {
        // no change and return directly
        return userVo;
    }
}