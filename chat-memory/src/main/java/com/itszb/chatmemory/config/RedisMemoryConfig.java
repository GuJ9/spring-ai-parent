package com.itszb.chatmemory.config;

import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.exceptions.JedisAccessControlException;

@Data
@Component
@ConfigurationProperties(value = "spring.ai.memory.redis")

public class RedisMemoryConfig {
    private String host;

    private Integer port;

    private String username;

    private String password;

    @Bean
    @Primary
    public JedisRedisChatMemoryRepository redisChatMemoryRepositoty(){
        return JedisRedisChatMemoryRepository.builder().host(host).port(port).build();
    }

}
