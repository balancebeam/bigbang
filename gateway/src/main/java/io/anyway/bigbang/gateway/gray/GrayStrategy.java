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

    private List<UserDefinition> uatList = Collections.emptyList();

    private List<WeightDefinition> wgtList= Collections.emptyList();

    private String defGroup;

    @Getter
    @Setter
    @ToString
    public static class UserDefinition {
        private List<Pattern> users;
        private String cluster;
    }

    @Getter
    @Setter
    @ToString
    public static class WeightDefinition{
        private int weight;
        private String cluster;
    }
}
