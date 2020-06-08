package com.example.aws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Amazons3Application {
	
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Amazons3Application.class);
    }

	public static void main(String[] args) {
		SpringApplication.run(Amazons3Application.class, args);
	}

}
