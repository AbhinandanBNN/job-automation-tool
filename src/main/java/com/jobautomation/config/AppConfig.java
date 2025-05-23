package com.jobautomation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private Naukri naukri = new Naukri();

    @Data
    public static class Naukri {
        private String username = System.getenv("NAUKRI_USERNAME");
        private String password = System.getenv("NAUKRI_PASSWORD");
        private String location = "Bangalore";
        private String skills = "Java,Spring Boot,Microservices,REST API";
        private String experience = "3";
    }
}
