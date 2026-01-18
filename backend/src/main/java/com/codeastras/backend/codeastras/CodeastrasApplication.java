package com.codeastras.backend.codeastras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeastrasApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeastrasApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
		return new com.fasterxml.jackson.databind.ObjectMapper()
				.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
	}
}
