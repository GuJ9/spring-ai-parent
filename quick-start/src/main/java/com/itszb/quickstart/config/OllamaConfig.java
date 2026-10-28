package com.itszb.quickstart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.ollama")
public class OllamaConfig {
    private String apiKey;
    private String baseUrl;
    private String model;

}


