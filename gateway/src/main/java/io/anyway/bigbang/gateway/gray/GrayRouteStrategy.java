package io.anyway.bigbang.gateway.gray;

import io.anyway.bigbang.framework.discovery.GrayRouteContext;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
public class GrayRouteStrategy {

    private List<UserDefinition> uatList = Collections.EMPTY_LIST;

    private List<WeightDefinition> wgtList= Collections.EMPTY_LIST;

    private String defaultCluster= "DEFAULT";

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
