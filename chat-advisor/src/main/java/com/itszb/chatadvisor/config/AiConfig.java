package com.itszb.chatadvisor.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.itszb.chatadvisor.advisor.LoggingAdvisor;
import com.itszb.chatadvisor.advisor.TokenGuardAdvisor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Array;
import java.util.ArrayList;

@RequiredArgsConstructor
@Configuration
public class AiConfig {

    @Autowired
    private  DashScopeChatModel dashScopeChatModel;

    @Autowired
    private  LoggingAdvisor loggingAdvisor;

    @Autowired
    private TokenGuardAdvisor tokenGuardAdvisor;

    @Bean
    public ChatClient chatClient() {
        ArrayList<String> list = new ArrayList<>();
        list.add("猪");
        SafeGuardAdvisor guardAdvisor = SafeGuardAdvisor.builder().sensitiveWords(list).build();
        return ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(loggingAdvisor, tokenGuardAdvisor, guardAdvisor)
                .build();
    }
}
