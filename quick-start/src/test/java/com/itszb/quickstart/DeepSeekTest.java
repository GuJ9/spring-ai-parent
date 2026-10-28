package com.itszb.quickstart;

import com.itszb.quickstart.config.DeepSeekConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class DeepSeekTest {
    @Autowired
    private DeepSeekConfig config;
    @Test
    void test(){
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model(DeepSeekApi.DEFAULT_CHAT_MODEL).temperature(0.8).build();
        DeepSeekApi deepSeekApi = DeepSeekApi.builder().apiKey(config.getApiKey()).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(options).build();
        String call = chatModel.call("什么是猫咪");
        System.out.println(call);

    }
    @Test
    void streamTest(){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder().apiKey(config.getApiKey()).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build();
        Flux<String> stream = chatModel.stream("什么是猫咪");
        stream.toIterable().forEach(System.out::println);

    }

    @Test
    void stopTest(){
        List<String> stop = new ArrayList<>();
        stop.add("聚光灯");
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model(DeepSeekApi.DEFAULT_CHAT_MODEL).temperature(0.7).stop(stop).build();
        DeepSeekApi deepSeekApi = DeepSeekApi.builder().apiKey(config.getApiKey()).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(options).build();
        String call = chatModel.call("假设你是一个演员，怎么描述自己");
        System.out.println(call);

    }

    @Test
    void thinkTest(){

        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER).build();
        DeepSeekApi deepSeekApi = DeepSeekApi.builder().apiKey(config.getApiKey()).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(options).build();
        ChatResponse chatResponse = chatModel.call(new Prompt("你是谁"));
        DeepSeekAssistantMessage output = (DeepSeekAssistantMessage) chatResponse.getResult().getOutput();
        System.out.println(output.getReasoningContent());
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println(output.getText());

    }
}
