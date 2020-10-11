package io.anyway.bigbang.framework.gray;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GrayContext {
    final public static String GRAY_NAME = "x-gray";
    private String group;
    private String defGroup;
}
