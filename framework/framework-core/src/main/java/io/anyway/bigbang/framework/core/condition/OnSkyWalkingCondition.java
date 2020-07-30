package io.anyway.bigbang.framework.core.condition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


@Slf4j
class OnSkyWalkingCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        try {
            getClass().getClassLoader().loadClass("org.apache.skywalking.apm.agent.core.context.TracingContext");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}


