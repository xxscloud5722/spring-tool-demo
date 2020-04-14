package com.github.xxscloud.demo.logx;



import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

/**
 * @author Cat.
 */
public interface LogResolver {

    /**
     * AOP 参数序列化.
     *
     * @param log       配置对象.
     * @param item      对象.
     * @param throwable 异常.
     * @return 序列化后的字符串.
     */
    String supportsParameter(@NonNull Log log, @Nullable Object item, @Nullable Throwable throwable);

    /**
     * AOP 执行完成后回调方法.
     *
     * @param log       配置对象.
     * @param method    方法信息.
     * @param startTime 开始时间.
     * @param endTime   结束时间.
     * @param request   序列化的String.
     * @param response  序列化后的String.
     * @param throwable 异常对象.
     */
    void resolve(@NonNull Log log, Method method, long startTime, long endTime, @NonNull StringBuilder request, @NonNull StringBuilder response, @Nullable Throwable throwable);
}
