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

    private List<TesterDefinition> uatList = Collections.EMPTY_LIST;

    private List<WeightDefinition> wgtList= Collections.EMPTY_LIST;

    private GrayRouteContext defaultContext;

    @Getter
    @Setter
    @ToString
    public static class TesterDefinition extends GrayRouteContext {
        private List<Pattern> testers;
    }

    @Getter
    @Setter
    @ToString
    public static class WeightDefinition extends GrayRouteContext{
        private int weight;
    }

}
