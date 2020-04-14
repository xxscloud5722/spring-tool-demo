package com.github.xxscloud.demo.spring;


import org.springframework.context.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author Cat.
 * 快速获取Spring 上下文和发布Spring 事件.
 */
public final class SpringContextUtils implements ApplicationContextAware, ApplicationEventPublisherAware, ResourceLoaderAware {

    private static ApplicationContext applicationContext = null;
    private static ApplicationEventPublisher applicationEventPublisher = null;
    private static ResourceLoader resourceLoader = null;


    @Nullable
    public static <T> T getBean(Class<T> clazz) {
        if (SpringContextUtils.applicationContext != null) {
            return SpringContextUtils.applicationContext.getBean(clazz);
        }
        return null;
    }

    public static boolean publishEvent(ApplicationEvent applicationEvent) {
        SpringContextUtils.applicationEventPublisher.publishEvent(applicationEvent);
        return true;
    }


    @Nullable
    public static <T> ClassMetadata getSubclassFirst(final Class<T> clazz) {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);

        try {
            Resource[] resources = resolver.getResources("classpath:**/*.class");
            for (Resource r : resources) {
                MetadataReader reader = metaReader.getMetadataReader(r);
                for (String interfaceName : reader.getClassMetadata().getInterfaceNames()) {
                    if (Objects.equals(interfaceName, clazz.getName())) {
                        return reader.getClassMetadata();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static <T> T findDefaultBean(Class<T> clazz) {
        if (SpringContextUtils.applicationContext != null) {
            final Map<String, T> map = SpringContextUtils.applicationContext.getBeansOfType(clazz);
            for (Map.Entry<String, T> item : map.entrySet()) {
                return item.getValue();
            }
        }
        return null;
    }


    @Override
    public void setApplicationContext(@NonNull final ApplicationContext applicationContext) {
        SpringContextUtils.applicationContext = applicationContext;
    }

    @Override
    public void setApplicationEventPublisher(@NonNull final ApplicationEventPublisher applicationEventPublisher) {
        SpringContextUtils.applicationEventPublisher = applicationEventPublisher;
    }


    @Override
    public void setResourceLoader(@NonNull final ResourceLoader resourceLoader) {
        SpringContextUtils.resourceLoader = resourceLoader;
    }
}