package com.mclamee.tools.tests.sanitizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.mclamee.tools.sanitizer.util.WhiteSpaceUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SanitizerTestConfig.class)
@Slf4j
public class SanitizerTest {

    @Autowired
    private TestControllerBean controller;

    private List<TestUserVo> userVos;
    private TestUserVo userVo;

    @Before
    public void setup() {
        log.info("controller = " + controller);

        userVos = new ArrayList<>();
        userVos.add(new TestUserVo("william test"));

        userVo = new TestUserVo("wicky test test");
    }

    @Test
    public void testDefault() {
        // trigger aspect
        List<TestUserVo> result = controller.callDefaultSanitizer(this.userVos);

        // asserts
        Assert.assertTrue(result != null);
        Assert.assertTrue(result.stream().allMatch(r -> r.getUserName().equals("default")));
    }

    @Test
    public void testCallByMethod() {
        // trigger aspect
        List<TestUserVo> result = controller.callSanitizerByMethodName(this.userVos);

        // asserts
        Assert.assertTrue(result != null);
        Assert.assertTrue(result.stream().allMatch(r -> r.getUserName().equals("byMethod")));
    }

    @Test
    public void testSpecial() {
        // trigger aspect
        TestControllerBean.Result results = controller.callSpecialSanitizer(userVos, userVo);

        // asserts for Vos
        List<TestUserVo> result = results.getUserVos();
        Assert.assertTrue(result != null);
        Assert.assertTrue(result.stream().allMatch(r -> r.getUserName().equals("william")));

        // asserts for Vo
        TestUserVo resultVo = results.getUserVo();
        Assert.assertTrue(resultVo != null);
        Assert.assertTrue(resultVo.getUserName().equals("changed"));
    }

    @Test
    public void testInvalid() {
        // trigger aspect
        TestControllerBean.Result results = controller.callInvalidSanitizer(userVos, userVo);

        // invalid name, using default com.mclamee.sanitizer
        List<TestUserVo> result = results.getUserVos();
        Assert.assertTrue(result != null);
        Assert.assertTrue(result.stream().allMatch(r -> r.getUserName().equals("default")));

        // asserts for Vo
        TestUserVo resultVo = results.getUserVo();
        Assert.assertTrue(resultVo != null);
        Assert.assertTrue(resultVo.getUserName().equals("changed"));
    }

    @Test
    public void testNonExists() {
        // trigger aspect
        try {
            controller.callNonExistsSanitizer(new TestControllerBean());
            Assert.fail("Expecting Exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asserts for non-exists
            Assert.assertEquals("No default Sanitizer found for the 0th arg of callNonExistsSanitizer", e.getMessage());
        }
    }

    @Test
    public void testLoopCall() {
        // trigger aspect
        TestUserVo resultVo = controller.loopCall(this.userVo);

        // asserts, user name no change
        Assert.assertTrue(resultVo != null);
        Assert.assertTrue(resultVo.getUserName().equals("wicky test test"));
    }

    @Test
    public void testWhiteSpaceUtil() {
        BiConsumer<Object, Object> asserts = (expected, actual) -> {
            if (Objects.equals(expected, actual)) {
                return;
            } else {
                throw new AssertionError("expected = [" + expected + "], actual = [" + actual + "]");
            }
        };

        String strAaa = "  \ta  a\ta   \r\n \u0020\u0020bb\u00a0b\t\u00a0";
        String expAaa1 = "a a a bb b";
        String expAaa2 = "a a a" + System.lineSeparator() + "bb b";
        String expAaa3 = "a a a  bb b";

        String className = WhiteSpaceUtil.class.getSimpleName();
        log.info("Start Testing " + className + "!");

        asserts.accept(expAaa1, WhiteSpaceUtil.sanitize(strAaa));
        asserts.accept(expAaa2, WhiteSpaceUtil.sanitize(strAaa, false));
        asserts.accept(expAaa3, WhiteSpaceUtil.sanitizeLine(strAaa));

        log.info("All " + className + " Tests Passed!");
    }
}
