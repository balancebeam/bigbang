package io.anyway.bigbang.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
//@EnableDiscoveryClient
@MapperScan("io.anyway.bigbang.example.dao")
@SpringBootApplication
public class ExampleBootStrapApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleBootStrapApplication.class, args);
	}

}
