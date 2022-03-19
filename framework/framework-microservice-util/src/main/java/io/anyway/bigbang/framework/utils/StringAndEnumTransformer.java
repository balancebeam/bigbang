package io.anyway.bigbang.framework.utils;

import io.anyway.bigbang.framework.model.enumeration.EnumStatement;
import lombok.Getter;
import lombok.Setter;

public class StringAndEnumTransformer implements TypeCopyTransformer<String, EnumStatement<String>>{

    @Getter
    @Setter
    private Class targetClass;

    @Override
    public EnumStatement<String> copySourceToTarget(String source) {
        return EnumStatement.of(getTargetClass(),source);
    }

    @Override
    public String copyTargetToSource(EnumStatement<String> target) {
        if(target==null) {
            return null;
        }
        return target.getCode();
    }
}
