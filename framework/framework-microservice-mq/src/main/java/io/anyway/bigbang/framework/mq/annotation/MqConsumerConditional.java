package io.anyway.bigbang.framework.mq.annotation;

import io.anyway.bigbang.framework.mq.config.MqConsumerCondition;
import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Conditional(MqConsumerCondition.class)
public @interface MqConsumerConditional {

    MqTypeEnum value();
}
