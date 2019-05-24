package com.mclamee.sanitizer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a parameter can be sanitized by Sanitizers
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sanitized {
    String value() default SanitizerCache.DEFAULT_KEY;
}