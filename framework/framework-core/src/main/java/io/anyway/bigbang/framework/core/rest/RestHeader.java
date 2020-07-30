package io.anyway.bigbang.framework.core.rest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RestHeader {
    private long errorCode = 0;
    private String msg = "success";

}