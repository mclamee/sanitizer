package com.mclamee.sanitizer;

import java.lang.reflect.Modifier;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Post processor to look for Sanitizers in all Beans
 */
@Slf4j
public class SanitizerPostProcessor implements BeanPostProcessor {

    @Autowired
    private SanitizerCache caches;

    /**
     * Before init
     *
     * @param bean bean
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
     * @param bean bean
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
                if (method.getParameterTypes().length != 1) {
                    throw new IllegalArgumentException("Sanitizer method can accept only 1 parameter");
                }
                String paraType = method.getGenericParameterTypes()[0].getTypeName();
                String returnType = method.getGenericReturnType().getTypeName();
                if (!Objects.equals(paraType, returnType)) {
                    throw new IllegalArgumentException("Sanitizer must return the same type as the parameter");
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

                System.out.println("Caching.. refKey = " + refKey);
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
                    System.out.println("Caching.. defaultKey = " + defaultKey);
                    method.setAccessible(true); // make it public
                    caches.put(defaultKey, method);
                }
            }
        });
        return bean;
    }
}