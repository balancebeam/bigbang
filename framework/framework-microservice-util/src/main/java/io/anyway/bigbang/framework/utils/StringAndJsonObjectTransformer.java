package io.anyway.bigbang.framework.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

public class StringAndJsonObjectTransformer implements TypeCopyTransformer<String,Object>{

    @Getter
    @Setter
    private Class targetClass;

    @Override
    public Object copySourceToTarget(String source) {
        if(StringUtils.isEmpty(source)) {
            return null;
        }
        return JsonUtil.fromString2Object(source,getTargetClass());
    }

    @Override
    public String copyTargetToSource(Object target) {
        if(target==null) {
            return null;
        }
        return JsonUtil.fromObject2String(target);
    }
}
