package io.anyway.bigbang.framework.model.enumeration;

public interface EnumStatement {

    String getCode();

    default String getMessage(){
        return getCode();
    }

    static <T extends EnumStatement> T of(Class<T> enumClass, String code){
        for (T each : enumClass.getEnumConstants()){
            if(each.getCode().equals(code)){
                return each;
            }
        }
        return null;
    }

}
