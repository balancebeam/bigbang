package io.anyway.bigbang.framework.tenant.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
public class Tenant {
    private String token;
    private String name;
    private List<String> classpathUrls= Collections.emptyList();

}
