package io.anyway.bigbang.framework.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface TypeCopyTransformer<SOURCE,TARGET> {

    TARGET copySourceToTarget(SOURCE source);

    SOURCE copyTargetToSource(TARGET target);

    default void setTargetClass(Class target){
    }

    default Class getTargetClass(){
        Type types = getClass().getGenericSuperclass();
        Type[] genericType = ((ParameterizedType) types).getActualTypeArguments();
        if(genericType==null || genericType.length<2){
            return null;
        }
        Type target =genericType[1];
        return target.getClass();
    }

    default void setSourceClass(Class source){
    }

    default Class getSourceClass(){
        Type types = getClass().getGenericSuperclass();
        Type[] genericType = ((ParameterizedType) types).getActualTypeArguments();
        if(genericType==null || genericType.length<2){
            return null;
        }
        Type target =genericType[0];
        return target.getClass();
    }

}
