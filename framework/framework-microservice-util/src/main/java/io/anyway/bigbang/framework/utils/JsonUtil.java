package io.anyway.bigbang.framework.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;


import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
final public class JsonUtil {

    private JsonUtil(){
    }

    private static ObjectMapper objectMapper= new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static Map<String,Object> fromString2Map(String json){
        try {
            return objectMapper.readValue(json,Map.class);
        } catch (JsonProcessingException e) {
            log.error("parse the json {} error",json,e);
        }
        return Collections.EMPTY_MAP;
    }

    public static <T> T fromString2Object(String json, Class<T> valueType){
        try {
            return objectMapper.readValue(json,valueType);
        } catch (Exception e) {
            log.error("parse the json {} error",json,e);
        }
        return null;
    }

    public static <T> T fromStream2Object(InputStream in, Class<T> valueType){
        try {
            return objectMapper.readValue(in,valueType);
        } catch (Exception e) {
            log.error("parse the inputStream error",e);
        }
        return null;
    }

    public static String fromObject2String(Object object){
        if(object== null){
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("to {} json string error",object,e);
        }
        return "{}";
    }

    public static <O,D> List<D> fromListObject2ListObject(List<O> oList){
        try {
            List<D> result = objectMapper.readValue(
                    objectMapper.writeValueAsString(oList),
                    new TypeReference<List<D>>() {
                    });
            return result;
        } catch (JsonProcessingException e) {
            log.error("oList {} to List Object error",oList,e);
        }
        return Collections.EMPTY_LIST;
    }

    public static <O,D> D fromObject2Object(O object){
        try {
            D dest = objectMapper.readValue(
                    objectMapper.writeValueAsString(object), new TypeReference<D>(){});
            return dest;
        } catch (JsonProcessingException e) {
            log.error("object {} to Object error",object,e);
        }
        return null;
    }

    public static ObjectMapper getObjectMapper(){
        return objectMapper;
    }

}