package com.mclamee.tools.sanitizer.config;

import com.mclamee.tools.sanitizer.SanitizerCache;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {SanitizerCache.BASE_PACKAGE})
public class SanitizerConfiguration {

}
