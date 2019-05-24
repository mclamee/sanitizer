package com.mclamee.sanitizer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import javax.annotation.PostConstruct;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.stereotype.Component;

/**
 * Cache Key to hold all the found Sanitizers
 */
@Slf4j
@Component
public class SanitizerCache {
    public static final String BASE_PACKAGE = "com.mclamee.sanitizer";
    public static final String DEFAULT_KEY = "default";

    private Map<SanitizerCacheKey, Method> cacheHolder;

    @PostConstruct
    public void init() {
        cacheHolder = new HashMap<>();
    }

    @Data
    @EqualsAndHashCode(exclude = "methodName")
    @Builder
    public static class SanitizerCacheKey implements Serializable {
        private String genericTypeName;
        private String sanitizerName;
        private String className;
        private String methodName;
    }

    public boolean exists(SanitizerCacheKey refKey) {
        return cacheHolder.get(refKey) != null;
    }

    public Method get(SanitizerCacheKey refKey, boolean isDefault) {
        ArrayList<BiPredicate<SanitizerCacheKey, SanitizerCacheKey>> predicates = new ArrayList<>();
        if (isDefault) {
            addDefaultPredicates(predicates);
        } else {
            addPredicates(predicates);
        }

        // lookup the method
        Set<Map.Entry<SanitizerCacheKey, Method>> entries = cacheHolder.entrySet();
        for (int i = 0; i < predicates.size(); i++) {
            BiPredicate<SanitizerCacheKey, SanitizerCacheKey> predicate = predicates.get(i);

            Optional<Map.Entry<SanitizerCacheKey, Method>> found;
            if ((found = testCacheKey(refKey, entries, predicate)).isPresent()) {
                log.debug("Found sanitizer [" + refKey + "] by rule#" + i + "!");
                return found.get().getValue();
            }
        }
        // nothing fond return null
        log.debug("No sanitizer [" + refKey + "] found!");
        return null;
    }

    private void addPredicates(ArrayList<BiPredicate<SanitizerCacheKey, SanitizerCacheKey>> predicates) {
        // 1. Type + className + sanitizerName
        BiPredicate<SanitizerCacheKey, SanitizerCacheKey> case1 = (key, ref) ->
            new EqualsBuilder()
                .append(key.getGenericTypeName(), ref.getGenericTypeName())
                .append(key.getClassName(), ref.getClassName())
                .append(key.getSanitizerName(), ref.getSanitizerName())
                .isEquals();
        predicates.add(case1);
        // 2. Type + sanitizerName
        BiPredicate<SanitizerCacheKey, SanitizerCacheKey> case2 = (key, ref) ->
            new EqualsBuilder()
                .append(key.getGenericTypeName(), ref.getGenericTypeName())
                .append(key.getSanitizerName(), ref.getSanitizerName())
                .isEquals();
        predicates.add(case2);
        // 3. Type + className + methodName
        BiPredicate<SanitizerCacheKey, SanitizerCacheKey> case3 = (key, ref) ->
            new EqualsBuilder()
                .append(key.getGenericTypeName(), ref.getGenericTypeName())
                .append(key.getClassName(), ref.getClassName())
                .append(key.getMethodName(), ref.getMethodName())
                .isEquals();
        predicates.add(case3);
        // 4. Type + methodName
        BiPredicate<SanitizerCacheKey, SanitizerCacheKey> case4 = (key, ref) ->
            new EqualsBuilder()
                .append(key.getGenericTypeName(), ref.getGenericTypeName())
                .append(key.getMethodName(), ref.getMethodName())
                .isEquals();
        predicates.add(case4);
    }

    private void addDefaultPredicates(ArrayList<BiPredicate<SanitizerCacheKey, SanitizerCacheKey>> predicates) {
        // 5. Type + className + default
        BiPredicate<SanitizerCacheKey, SanitizerCacheKey> case5 = (key, ref) ->
            new EqualsBuilder()
                .append(key.getGenericTypeName(), ref.getGenericTypeName())
                .append(key.getClassName(), ref.getClassName())
                .append(key.getSanitizerName(), DEFAULT_KEY)
                .isEquals();
        predicates.add(case5);
        // 6. Type + default
        BiPredicate<SanitizerCacheKey, SanitizerCacheKey> case6 = (key, ref) ->
            new EqualsBuilder()
                .append(key.getGenericTypeName(), ref.getGenericTypeName())
                .append(key.getSanitizerName(), DEFAULT_KEY)
                .isEquals();
        predicates.add(case6);
    }

    private Optional<Map.Entry<SanitizerCacheKey, Method>> testCacheKey(
        SanitizerCacheKey refKey, Set<Map.Entry<SanitizerCacheKey, Method>> entries,
        BiPredicate<SanitizerCacheKey, SanitizerCacheKey> predicate) {
        return entries.stream().filter(k -> {
            SanitizerCacheKey key = k.getKey();
            return predicate.test(key, refKey);
        }).findFirst();
    }

    public void put(SanitizerCacheKey refKey, Method method) {
        this.cacheHolder.put(refKey, method);
    }
}
