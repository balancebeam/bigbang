package io.anyway.bigbang.framework.mq.config;

import io.anyway.bigbang.framework.mq.annotation.MqConsumerConditional;
import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public class MqConsumerCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        MultiValueMap<String, Object> valueMap= metadata.getAllAnnotationAttributes(MqConsumerConditional.class.getName());
        MqTypeEnum mqTypeEnum= (MqTypeEnum)valueMap.getFirst("value");
        if(mqTypeEnum==null){
            return ConditionOutcome.noMatch("Didn't config mq type.");
        }
        Environment env = context.getEnvironment();
        String prefix = "spring.reliable-mq.client."+mqTypeEnum.getCode()+".";
        String serverAddress= env.getProperty(prefix+"server-address");
        if(StringUtils.isEmpty(serverAddress) ){
            return ConditionOutcome.noMatch("Didn't config server-address");
        }
        return ConditionOutcome.match();
    }

}
