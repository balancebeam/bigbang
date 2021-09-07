package io.anyway.bigbang.gateway.gray;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
public class GrayStrategyDefintion {

    private List<UserDefinition> userList = Collections.emptyList();

    private List<HeaderDefinition> headerList = Collections.emptyList();

    private List<WeightDefinition> wgtList= Collections.emptyList();

    Map<String,Map<String, String>> headerMapping= new HashMap<>();

    @Getter
    @Setter
    @ToString
    public static class UserDefinition {
        private List<Pattern> users;
        private String version;
    }

    @Getter
    @Setter
    @ToString
    public static class HeaderDefinition {
        private Map<String,String> mapping;
        private String version;
    }

    @Getter
    @Setter
    @ToString
    public static class WeightDefinition{
        private int weight;
        private String version;
    }
}
