package io.anyway.bigbang.framework.mqclient.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PurgeJobContext {
    private Integer monthAhead;
    private PurgeJobType purgeJobType;
}
