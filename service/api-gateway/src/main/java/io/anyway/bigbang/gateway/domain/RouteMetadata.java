package io.anyway.bigbang.gateway.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RouteMetadata {

    private String id;
    private String uri;
    private String path;
    private boolean rewritePath;
    private String regexp= "";
    private String replacement= "";
}
