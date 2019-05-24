package com.mclamee.sanitizer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Enable the Sanitizer Feature
 */
@Slf4j
@Aspect
@Component
public class SanitizerAspect {
    public static final String AROUND_EXPRESSION = "execution(public * *(.., @" + SanitizerCache.BASE_PACKAGE + ".Sanitized (*), ..))";

    @Around(AROUND_EXPRESSION)
    public Object methodWithAnnotationOnAtLeastOneParameter(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getSignature().getDeclaringTypeName();

        MethodSignature methodSig = (MethodSignature) pjp.getSignature();
        Method targetMethod = methodSig.getMethod();
        Annotation[][] annotations = targetMethod.getParameterAnnotations();
        Object[] args = pjp.getArgs();

        for (int i = 0; i < args.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (Sanitized.class.isInstance(annotation)) {
                    Object targetParam = args[i];

                    String methodName = targetMethod.getName();
                    String genericTypeName = targetMethod.getGenericParameterTypes()[i].getTypeName();
                    String sanitizerName = ((Sanitized) annotation).value();

                    SanitizerCache.SanitizerCacheKey refKey = SanitizerCache.SanitizerCacheKey.builder()
                        .className(className)
                        .genericTypeName(genericTypeName)
                        .sanitizerName(sanitizerName)
                        .methodName(sanitizerName) // cannot get method name, so use sanitizer name instead
                        .build();

                    Method sanitizer = lookupMethod(i, methodName, refKey);

                    if (sanitizer != null) {
                        log.info("Invoking Sanitizer [" + refKey + "] on the " + i + "th arg of " + methodName + ", value = " + targetParam);
                        Object sanitized = sanitizer.invoke(new Object(), targetParam);
                        log.info("Sanitized value = " + sanitized);
                        // replace the previous args
                        args[i] = sanitized;
                    }
                }
            }
        }

        return pjp.proceed();
    }

    @Autowired
    private SanitizerCache cache;

    private Method lookupMethod(int i, String methodName, SanitizerCache.SanitizerCacheKey refKey) {
        Method sanitizer = cache.get(refKey, false);
        if (sanitizer == null) {
            log.warn("No Sanitizer found by Key [" + refKey + "] for the " + i + "th arg of " + methodName + "");

            sanitizer = cache.get(refKey, true);
            if (sanitizer == null) {
                throw new IllegalArgumentException("No default Sanitizer found for the " + i + "th arg of " + methodName + "");
            } else {
                log.info("Using Default Sanitizer: [" + sanitizer + "]");
            }
        }
        return sanitizer;
    }

}
