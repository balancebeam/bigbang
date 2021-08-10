package io.anyway.bigbang.framework.bootstrap.config;



import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;


@Configuration
@ImportAutoConfiguration({RestTemplateConfigure.class})
public class BootstrapConfigure {

}
