package com.itszb.quickstart.controller;

import com.itszb.quickstart.config.DeepSeekConfig;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RequestMapping("/ai")
@RestController
public class DeepSeekController {
    @Autowired
    private DeepSeekConfig config;

    @GetMapping("/chat/{message}")
    public String chat(@PathVariable String message){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder().apiKey(config.getApiKey()).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build();
        Prompt prompt = new Prompt(message);
        ChatResponse chatResponse = chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getText();

    }
    @PostMapping("/chatStream")
    public SseEmitter chatStream(@RequestBody String message){
        SseEmitter emitter = new SseEmitter();

        //调用Deepseek接口——》数据——》emitter——》前端
        DeepSeekApi deepSeekApi = DeepSeekApi.builder().apiKey(config.getApiKey()).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build();
        Prompt prompt = new Prompt(message);
        Flux<ChatResponse> stream = chatModel.stream(prompt);
        stream.subscribe(chunk->{
            try {
                String content = chunk.getResult().getOutput().getText();
                emitter.send(SseEmitter.event().data(content).build());
            }catch(Exception e){
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

}
