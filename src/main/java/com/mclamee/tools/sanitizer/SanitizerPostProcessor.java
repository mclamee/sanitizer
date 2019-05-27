package com.mclamee.tools.sanitizer;

import java.lang.reflect.Modifier;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * Post processor to look for Sanitizers in all Beans
 */
@Slf4j
@Component
public class SanitizerPostProcessor implements BeanPostProcessor {

    @Autowired
    private SanitizerCache caches;

    /**
     * Before init
     *
     * @param bean     bean
     * @param beanName beanName
     * @return the object
     * @throws BeansException exception
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Post init
     *
     * @param bean     bean
     * @param beanName beanName
     * @return the object
     * @throws BeansException exception
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Sanitizer annotation = AnnotationUtils.getAnnotation(method, Sanitizer.class);
            if (annotation != null) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException("Sanitizer method must be static: " + method);
                }
                int len = method.getParameterTypes().length;
                if (len <= 0) {
                    throw new IllegalArgumentException("Sanitizer method must accept one parameter but no parameter " +
                        "found: " + method);
                }
                if (len > 1) {
                    throw new IllegalArgumentException("Sanitizer method can only accept one parameter but " + len +
                        " parameters found: " + method);
                }
                String paraType = method.getGenericParameterTypes()[0].getTypeName();
                String returnType = method.getGenericReturnType().getTypeName();
                if (!Objects.equals(paraType, returnType)) {
                    throw new IllegalArgumentException("Sanitizer method must return the same type as the parameter:" +
                        " " + method);
                }

                String className = targetClass.getName();
                String methodName = method.getName();
                String sanitizerName = annotation.name();
                // default using method name
                boolean noNameSpecified = false;
                if (StringUtils.isEmpty(sanitizerName)) {
                    noNameSpecified = true;
                    sanitizerName = methodName;
                }

                SanitizerCache.SanitizerCacheKey refKey = SanitizerCache.SanitizerCacheKey.builder()
                    .sanitizerName(sanitizerName)
                    .genericTypeName(paraType)
                    .className(className)
                    .methodName(methodName)
                    .build();

                log.info("Caching.. refKey = " + refKey);
                if (caches.exists(refKey)) {
                    throw new IllegalArgumentException("Duplicated Sanitizer by Key: [" + refKey + "], please specify a name for it.");
                } else {
                    method.setAccessible(true); // make it public
                    caches.put(refKey, method);
                }

                SanitizerCache.SanitizerCacheKey defaultKey = SanitizerCache.SanitizerCacheKey.builder()
                    .sanitizerName(SanitizerCache.DEFAULT_KEY)
                    .genericTypeName(paraType)
                    .className(className)
                    .methodName(methodName)
                    .build();

                // override or use the first found as default
                if (noNameSpecified && (!caches.exists(defaultKey) || annotation.setDefault())) {
                    log.info("Caching.. defaultKey = " + defaultKey);
                    method.setAccessible(true); // make it public
                    caches.put(defaultKey, method);
                }
            }
        });
        return bean;
    }
}