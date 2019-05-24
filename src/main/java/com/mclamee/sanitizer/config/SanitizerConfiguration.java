package com.mclamee.sanitizer.config;

import com.mclamee.sanitizer.SanitizerCache;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {SanitizerCache.BASE_PACKAGE})
public class SanitizerConfiguration {

}
