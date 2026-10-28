package com.itszb.chatclient.controller;

import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/client")
public class ChatClientController {
    @Autowired
    private DeepSeekChatModel deepSeekChatModel;

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody String message){
        SseEmitter emitter = new SseEmitter();
        //调用Deepseek接口
        ChatClient chatClient = ChatClient.builder(deepSeekChatModel).build();
        Flux<String> content = chatClient.prompt()
                //.system("你是一个律师，你现在只能回答法律问题，如果不是法律相关，就回复无可奉告")
                .user(message).stream().content();
        content.subscribe(chunk -> {
            try {

                emitter.send(SseEmitter.event().data(chunk).build());
            }catch (Exception e){
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);
        return emitter;

    }


}
