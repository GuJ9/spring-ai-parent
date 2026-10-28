package com.itszb.quickstart;

import com.itszb.quickstart.config.OllamaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
public class OllamaTest {

    @Autowired
    private OllamaConfig config;

    @Test
    void test(){
        OllamaApi ollamaApi = OllamaApi.builder().build();
        OllamaChatOptions chatOptions = OllamaChatOptions.builder().model(config.getModel()).build();

        OllamaChatModel ollamaChatModel = OllamaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(chatOptions).build();
        String content = ollamaChatModel.call("你是谁");
        System.out.println(content);
    }

    @Test
    void StreamTest(){
        OllamaApi ollamaApi = OllamaApi.builder().build();
        OllamaChatOptions chatOptions = OllamaChatOptions.builder().model(config.getModel()).build();
        OllamaChatModel chatModel = OllamaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(chatOptions).build();
        Flux<String> stream = chatModel.stream("你是谁");
        stream.toIterable().forEach(System.out::println);
    }
}
