package com.itszb.chatclient.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/prompt")
public class PromptController {

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        //调用Deepseek接口
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();
        Flux<String> content = chatClient.prompt()
                .system("你是一个老师，你现在只能回答教育问题，如果不是教育相关，就回复无可奉告")
                .user(message).stream().content();
        content.subscribe(chunk -> {
            try {

                emitter.send(SseEmitter.event().data(chunk).build());
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);
        return emitter;
    }

    @PostMapping("/chat2")
    public SseEmitter chat2(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        SystemMessage systemMessage = SystemMessage.builder().text("你是一个老师，你现在只能回答教育问题，如果不是教育相关，就回复无可奉告").build();
        UserMessage userMsg = UserMessage.builder().text(message).build();
        Prompt prompt = Prompt.builder().messages(systemMessage, userMsg).build();
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();
        Flux<String> content = chatClient.prompt(prompt).stream().content();
        content.subscribe(chunk -> {
            try {

                emitter.send(SseEmitter.event().data(chunk).build());
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);
        return emitter;
    }

    @PostMapping("/chat3")
    public SseEmitter chat3(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        PromptTemplate template = PromptTemplate.builder().template("你是一个{role}，讲一个关于{topic}的故事").build();
        Prompt prompt = template.create(Map.of("role", "讲故事助手", "topic", message));
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();
        Flux<String> content = chatClient.prompt(prompt).stream().content();
        content.subscribe(chunk -> {
            try {

                emitter.send(SseEmitter.event().data(chunk).build());
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);
        return emitter;
    }

    @PostMapping("/chat5")
    public SseEmitter chat5(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(systemPrompt)
                .build();
        //lambada表达式
        Flux<String> content = chatClient.prompt()
                .user(message)
                .stream().content();
        content.subscribe(chunk -> {
            try {
                emitter.send(SseEmitter.event().data(chunk).build());
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);
        return emitter;
    }

    @Value("classpath:files/prompt")
    private Resource  systemPrompt;
}
