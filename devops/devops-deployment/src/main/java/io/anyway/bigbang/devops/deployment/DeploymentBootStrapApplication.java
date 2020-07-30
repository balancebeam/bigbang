package io.anyway.bigbang.devops.deployment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeploymentBootStrapApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeploymentBootStrapApplication.class, args);
	}

}
