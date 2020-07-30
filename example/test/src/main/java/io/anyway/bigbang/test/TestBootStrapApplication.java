package io.anyway.bigbang.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TestBootStrapApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestBootStrapApplication.class, args);
	}

}
