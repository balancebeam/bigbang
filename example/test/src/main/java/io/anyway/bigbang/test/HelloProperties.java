package io.anyway.bigbang.test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ToString
@ConfigurationProperties("spring.bigbang.hello")
public class HelloProperties {

    private Map<String, Map<String,String>> map;
}
