package io.anyway.bigbang.framework.security.mask;

import io.anyway.bigbang.framework.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
final public class MaskSensitiveDataUtil {

    private MaskSensitiveDataUtil(){}

    public static String desensitizeString(String text,String pattern){
        if(StringUtils.isEmpty(text)){
            return text;
        }
        if(pattern== null){
            pattern= "{1}***";
        }
        pattern= pattern.trim();
        int prefixNum= 0,
                suffixNum= 0;
        if(pattern.startsWith("{")){
            int index= pattern.indexOf("}",2);
            try {
                String prefixStr = pattern.substring(1, index);
                prefixNum= Integer.parseInt(prefixStr.trim());
            }catch (Exception e){
                log.info("Illegal prefix mask pattern: {}",pattern,e);
            }
        }
        if(pattern.endsWith("}")){
            int index= pattern.lastIndexOf("{");
            try {
                String suffixStr = pattern.substring(index+1, pattern.length()-1);
                suffixNum= Integer.parseInt(suffixStr.trim());
            }catch (Exception e){
                log.info("Illegal suffix mask pattern: {}",pattern,e);
            }
        }
        int keepNum= prefixNum+suffixNum;
        if(text.length()<=keepNum){
            int overNum= keepNum- text.length()+1;
            if(overNum>suffixNum){
                suffixNum= 0;
                overNum-=suffixNum;
            }
            else{
                suffixNum-=overNum;
                overNum=0;
            }
            if(overNum>prefixNum){
                prefixNum= 0;
            }
            else{
                prefixNum-=overNum;
            }
        }
        StringBuilder builder= new StringBuilder();
        builder.append("(?<=[\\w|\\u4E00-\\u9FA5]{")
                .append(prefixNum)
                .append("})[\\w|\\u4E00-\\u9FA5](?=[\\w|\\u4E00-\\u9FA5]{")
                .append(suffixNum)
                .append("})");
        return text.replaceAll(builder.toString(),"*");
    }

    public static String fromObject2MaskJsonString(Object object){
        if(object== null){
            return null;
        }
        try {
            MaskSensitiveDataContextHolder.anchorExecutorMarkSensitive();
            return JsonUtil.fromObject2String(object);
        }
        finally {
            MaskSensitiveDataContextHolder.resetExecutorMarkSensitive();
        }
    }

    public static String fromObject2MaskArrayString(Object object){
        if(object==null){
            return null;
        }
        StringBuilder builder= new StringBuilder();
        builder.append("[");
        int index =0;
        PropertyDescriptor[] propertyDescriptorList= BeanUtils.getPropertyDescriptors(object.getClass());
        for(PropertyDescriptor each: propertyDescriptorList){
            if(each.getReadMethod()!= null && each.getWriteMethod()!=null){
                if(index!=0) {
                    builder.append(",");
                }
                Method readMethod= each.getReadMethod();
                ReflectionUtils.makeAccessible(readMethod);
                Object value= ReflectionUtils.invokeMethod(readMethod,object);
                if (Objects.equals (each.getPropertyType(), String.class)) {
                    Field field = ReflectionUtils.findField(object.getClass(), each.getName(), each.getPropertyType());
                    MaskSensitiveData maskSensitiveData = field.getAnnotation(MaskSensitiveData.class);
                    if (maskSensitiveData != null) {
                        value = desensitizeString((String) value, maskSensitiveData.value());
                    }
                }
                builder.append(value);
                index++;
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
