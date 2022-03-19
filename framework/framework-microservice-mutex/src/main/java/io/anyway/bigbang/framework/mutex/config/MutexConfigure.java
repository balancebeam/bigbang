package io.anyway.bigbang.framework.mutex.config;

import io.anyway.bigbang.framework.mutex.service.MutexAspectj;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"io.anyway.bigbang.framework.mutex.dao"})
public class MutexConfigure {

    @Bean
    public MutexAspectj createMutexAspectj(){
        return new MutexAspectj();
    }

}
