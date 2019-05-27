package com.mclamee.tools.sanitizer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Mark a static method as Sanitizer
 * The first sanitizer scanned for the return type will be used as Default Sanitizer
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sanitizer {
    boolean setDefault() default false;

    @AliasFor("value") String name() default "";

    @AliasFor("name") String value() default "";
}