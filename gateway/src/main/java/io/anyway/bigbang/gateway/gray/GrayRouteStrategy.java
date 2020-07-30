package io.anyway.bigbang.gateway.gray;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
public class GrayRouteStrategy {

    private Map<Pattern,String> operator = Collections.emptyMap();

    private Map<String,Integer> weight= Collections.emptyMap();

}
