package io.anyway.bigbang.framework.core.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnAmazonECSCondition.class)
public @interface ConditionalOnAmazonECS {
}
