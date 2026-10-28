package com.itszb.quickstart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.ai.deepseek")
public class DeepSeekConfig {
    private String apiKey;

}
