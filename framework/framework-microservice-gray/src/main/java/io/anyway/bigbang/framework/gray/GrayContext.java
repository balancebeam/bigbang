package io.anyway.bigbang.framework.gray;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
public class GrayContext {
    final public static String GRAY_NAME = "x-gray";
    private List<String> inVers= Collections.EMPTY_LIST;
    private List<String> exVers= Collections.EMPTY_LIST;
}
