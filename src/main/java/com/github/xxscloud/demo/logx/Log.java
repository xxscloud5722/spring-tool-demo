package com.github.xxscloud.demo.logx;

import java.lang.annotation.*;

/**
 * @author Cat.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String value() default "";
    String type() default "";
    String module() default "";
    String tag() default "";
}
