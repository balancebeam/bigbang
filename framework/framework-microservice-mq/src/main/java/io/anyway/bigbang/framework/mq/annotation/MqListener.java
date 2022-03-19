package io.anyway.bigbang.framework.mq.annotation;

import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqListener {
    /**
     * 消息中间件类型，默认为MqClientProperties中的mqType配置
     * @return
     */
    MqTypeEnum mqType() default MqTypeEnum.DEFAULT;

    /**
     * 消息监听地址
     * @return
     */
    String value();

    /**
     * 消息标识
     * @return
     */
    String[] tags() default {};

    /**
     * 分组名称
     * @return
     */
    String group() default "";
}
