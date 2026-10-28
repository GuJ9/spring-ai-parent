package com.itszb.chatmemory.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.MemoryManagerMXBean;

@Configuration
public class ChatClientConfig {

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private JdbcChatMemoryRepository jdbcChatMemoryRepository;

    @Autowired
    private JedisRedisChatMemoryRepository jedisRedisChatMemoryRepository;

    @Bean
    public ChatClient chatClient(){
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(dashScopeChatModel).defaultAdvisors(memoryAdvisor).build();
    }

    @Bean
    public ChatClient jdbcChatClient(){
        MessageWindowChatMemory windowChatMemory = MessageWindowChatMemory.builder().chatMemoryRepository(jdbcChatMemoryRepository).maxMessages(10).build();
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(dashScopeChatModel).defaultAdvisors(memoryAdvisor).build();
    }

    @Bean
    public ChatClient redisChatClient(){
        MessageWindowChatMemory windowChatMemory = MessageWindowChatMemory
                .builder().chatMemoryRepository(jedisRedisChatMemoryRepository)
                .maxMessages(10).build();
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(dashScopeChatModel).defaultAdvisors(memoryAdvisor).build();
    }
}
