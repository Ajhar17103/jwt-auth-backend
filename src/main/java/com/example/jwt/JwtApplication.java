package com.example.jwt;

import com.example.jwt.confiq.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class JwtApplication {
	public static void main(String[] args) {
		SpringApplication.run(JwtApplication.class, args);
	}
}
