package io.anyway.bigbang.framework.security.mask;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.Objects;

public class MaskSensitiveDataSerializer extends JsonSerializer implements ContextualSerializer {

    private String pattern;

    public MaskSensitiveDataSerializer(){
        this("");
    }

    public MaskSensitiveDataSerializer(String pattern){
        this.pattern= pattern;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if(MaskSensitiveDataContextHolder.isExecutorMarkSensitive()){
            value= MaskSensitiveDataUtil.desensitizeString((String)value, pattern);
        }
        gen.writeString((String)value);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) { // 为空直接跳过
            if (Objects.equals (beanProperty.getType ().getRawClass (), String.class)) { // 非 String 类直接跳过
                MaskSensitiveData maskSensitiveData = beanProperty.getAnnotation (MaskSensitiveData.class);
                if (maskSensitiveData == null) {
                    maskSensitiveData = beanProperty.getContextAnnotation (MaskSensitiveData.class);
                }
                if (maskSensitiveData != null) {
                    return new MaskSensitiveDataSerializer(maskSensitiveData.value ());
                }
            }
            return serializerProvider.findValueSerializer (beanProperty.getType (), beanProperty);
        }
        return serializerProvider.findNullValueSerializer (beanProperty);
    }
}
