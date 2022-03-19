package io.anyway.bigbang.framework.model.enumeration;

public interface EnumStatement<T> {

    T getCode();

    default String getMessage(){
        return String.valueOf(getCode());
    }

    static <C extends EnumStatement,T> C of(Class<C> enumClass, T code){
        for (C each : enumClass.getEnumConstants()){
            if(each.getCode().equals(code)){
                return each;
            }
        }
        return null;
    }

}
