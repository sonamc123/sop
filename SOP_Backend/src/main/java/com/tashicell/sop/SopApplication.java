package com.tashicell.sop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication

public class SopApplication {

	public static void main(String[] args) {
		SpringApplication.run(SopApplication.class, args);

	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {

			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowedOrigins("http://10.70.91.90:3000","https://sop.tashicell.com");
				WebMvcConfigurer.super.addCorsMappings(registry);
			}
		};
	}
}
