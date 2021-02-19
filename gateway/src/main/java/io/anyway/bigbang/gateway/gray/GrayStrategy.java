package io.anyway.bigbang.gateway.gray;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
public class GrayStrategy {

    private List<UserDefinition> userList = Collections.emptyList();

    private List<HeaderDefinition> headerList = Collections.emptyList();

    private List<WeightDefinition> wgtList= Collections.emptyList();

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
        private Map<String,String> headers;
        private String name;
    }

    @Getter
    @Setter
    @ToString
    public static class WeightDefinition{
        private int weight;
        private String version;
    }
}
