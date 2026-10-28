package com.itszb.chatrag.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Autowired
    private DashScopeChatModel dashScopeChatModel;


    @Bean
    public ChatClient chatClient(){
        return ChatClient.builder(dashScopeChatModel).build();
    }
}
