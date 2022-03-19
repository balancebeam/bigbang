package io.anyway.bigbang.framework.utils;

import io.anyway.bigbang.framework.model.enumeration.EnumStatement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.util.ReflectionUtils.findField;


@Slf4j
public class BeanMapUtils {

    public static <T,E> E map(T source, Class<E> targetClass){

        try {
            if(null==source){
                log.info("source: {},targetClass:{}", source,targetClass);
                return null;
            }
            E target = targetClass.newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return null;

    }

    public static <T,E> E mixin(T source,E target){
        copyProperties(source, target);
        return target;
    }


    public static <T,E> List<E> mapList(List<T> sources, Class<E> targetClass){
        if(CollectionUtils.isEmpty(sources)){
            return Collections.emptyList();
        }

        try {
            List<E> result= new LinkedList<>();
            for(T each: sources) {
                E target = BeanUtils.instantiate(targetClass);
                copyProperties(each, target);
                result.add(target);
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return Collections.emptyList();

    }

//    private static ConcurrentMap<Class<? extends TypeCopyTransformer> , TypeCopyTransformer> TypeCopyTransformerMap = new ConcurrentHashMap<>();

    private static void copyProperties(Object source, Object target){

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        Class<?> actualEditable = target.getClass();

        PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);
        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null) {
                Class<?> sourceClass= source.getClass();
                String sourcePropertyName= targetPd.getName();
                PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(sourceClass, sourcePropertyName);
                if (sourcePd != null) {
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null){
                        Class<?> targetType= writeMethod.getParameterTypes()[0];
                        Class<?> sourceType= readMethod.getReturnType();
                        try {
                            if (ClassUtils.isAssignable(targetType,sourceType)) {
                                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                    readMethod.setAccessible(true);
                                }
                                Object value = readMethod.invoke(source);
                                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                    writeMethod.setAccessible(true);
                                }
                                writeMethod.invoke(target, value);
                                continue;
                            }
                            Field sourceField= findField(sourceClass, sourcePropertyName);
                            Field targetField= findField(actualEditable, sourcePropertyName);
                            TypeCopyTransformerAnnotation annotation= sourceField.getAnnotation(TypeCopyTransformerAnnotation.class);
                            if(annotation!= null
                                    && annotation.targetType()==targetField.getType()){
                                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                    readMethod.setAccessible(true);
                                }
                                Object sourceValue = readMethod.invoke(source);

                                TypeCopyTransformer transformer;
                                if(EnumStatement.class.isAssignableFrom(annotation.targetType())){
                                    transformer= new StringAndEnumTransformer();
                                }
                                else{
                                    transformer = annotation.transformer().newInstance();
                                }
                                transformer.setSourceClass(sourceField.getType());
                                transformer.setTargetClass(annotation.targetType());

                                Object targetValue= transformer.copySourceToTarget(sourceValue);
                                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                    writeMethod.setAccessible(true);
                                }
                                writeMethod.invoke(target, targetValue);
                                continue;
                            }
                            annotation= targetField.getAnnotation(TypeCopyTransformerAnnotation.class);
                            if(annotation!= null
                                    && annotation.targetType()==sourceField.getType()){
                                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                    readMethod.setAccessible(true);
                                }
                                Object targetValue = readMethod.invoke(source);

                                TypeCopyTransformer transformer;
                                if(EnumStatement.class.isAssignableFrom(annotation.targetType())){
                                    transformer= new StringAndEnumTransformer();
                                }
                                else{
                                    transformer = annotation.transformer().newInstance();
                                }
                                transformer.setSourceClass(targetField.getType());
                                transformer.setTargetClass(annotation.targetType());

                                Object sourceValue= transformer.copyTargetToSource(targetValue);
                                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                    writeMethod.setAccessible(true);
                                }
                                writeMethod.invoke(target, sourceValue);
                                continue;
                            }
                        } catch (Throwable ex) {
                            throw new FatalBeanException(
                                    "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                        }
                    }
                }
            }
        }
    }
}



