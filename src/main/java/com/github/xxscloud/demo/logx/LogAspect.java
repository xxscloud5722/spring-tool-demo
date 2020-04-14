package com.github.xxscloud.demo.logx;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import java.lang.reflect.Method;

import static com.github.xxscloud.demo.spring.SpringContextUtils.findDefaultBean;

/**
 * @author Cat.
 */
@Aspect
public class LogAspect {
    @Pointcut("@annotation(com.github.xxscloud.demo.logx.Log)")
    public void log() {

    }

    @Around(value = "log()")
    public Object afterReturn(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        //初始化参数
        final Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        final Log log = method.getAnnotation(Log.class);
        final LogResolver logResolver = findDefaultBean(LogResolver.class);
        final StringBuilder request = new StringBuilder();
        final StringBuilder response = new StringBuilder();

        //获取请求参数
        if (logResolver != null) {
            try {
                for (Object item : ((MethodInvocationProceedingJoinPoint) proceedingJoinPoint).getArgs()) {
                    final String result = logResolver.supportsParameter(log, item, null);
                    request.append(result == null ? "" : result);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        //执行方法
        final long startTime = System.currentTimeMillis();
        Throwable throwable = null;
        Object obj = null;
        try {
            obj = proceedingJoinPoint.proceed();
        } catch (Throwable ex) {
            throwable = ex;
        }
        final long endTime = System.currentTimeMillis();


        //执行LogResolver配置
        if (logResolver != null) {
            try {
                final String result = logResolver.supportsParameter(log, obj, throwable);
                response.append(result);
                logResolver.resolve(log, method, startTime, endTime, request, response, throwable);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //如果异常则抛出异常
        if (throwable != null) {
            throw throwable;
        }
        return obj;
    }
}
