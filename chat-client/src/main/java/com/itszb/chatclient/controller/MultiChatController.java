package com.itszb.chatclient.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.itszb.chatclient.vo.MultiChatReqVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.HashMap;

@RestController
@RequestMapping("/multi")
public class MultiChatController {
    HashMap<String, ChatModel> models = new HashMap<>();
    public MultiChatController(
            DeepSeekChatModel deepSeekChatModel,
            DashScopeChatModel dashScopechatModel,
            OllamaChatModel ollamaChatModel
    ){
        models.put("deepseek",deepSeekChatModel);
        models.put("dashscope",dashScopechatModel);
        models.put("ollama",ollamaChatModel);
    }
    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody MultiChatReqVo reqVo){
        SseEmitter emitter = new SseEmitter();
        String platform = reqVo.getPlatform();
        String model = reqVo.getModel();
        ChatModel chatModel = models.get(reqVo.getPlatform());
        ChatOptions options = ChatOptions.builder().model(model).build();
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultOptions(options)
                .build();
        Flux<String> content = chatClient.prompt(reqVo.getMessage()).stream().content();
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
