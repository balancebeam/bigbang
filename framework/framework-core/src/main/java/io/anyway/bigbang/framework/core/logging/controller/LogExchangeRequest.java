package io.anyway.bigbang.framework.core.logging.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Setter
@Getter
@ToString
public class LogExchangeRequest {

    @NotEmpty
    private String name;
    @NotEmpty
    private String level;
}
