package io.anyway.bigbang.regcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


@SpringBootApplication
@EnableEurekaServer
public class RegCenterBootStrapApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegCenterBootStrapApplication.class,args);
	}

}
