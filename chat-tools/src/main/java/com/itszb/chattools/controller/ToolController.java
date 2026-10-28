
package com.itszb.chattools.controller;

import com.itszb.chattools.tools.WeatherTool;
import com.itszb.chattools.tools.WeatherToolV3;
import com.itszb.chattools.tools.WeatherToolV4;
import com.itszb.chattools.vo.WeatherRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/tool")
@RequiredArgsConstructor
public class ToolController {

    @Autowired
    private  ChatClient chatClient;

    @Autowired
    private FunctionToolCallback <WeatherRequest, String>weatherToolCallback;

    @Autowired
    private WeatherToolV4 weatherToolV4;

    @PostMapping(
            value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat(@RequestBody String message){
        SseEmitter emitter = new SseEmitter(120_000L);

        if (message == null || message.isBlank()) {
            emitter.completeWithError(
                    new IllegalArgumentException("消息不能为空")
            );
            return emitter;
        }

        Flux<String> content = chatClient.prompt()
                .user(message)
                .tools(new WeatherTool())
                .stream()
                .content();
        content.subscribe(
                        chunk -> {
                            try {
                                emitter.send(
                                        SseEmitter.event()
                                                .data(chunk)
                                                .build()
                                );
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }

    @PostMapping(
            value = "/chat2",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat2(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter(120_000L);

        if (message == null || message.isBlank()) {
            emitter.completeWithError(
                    new IllegalArgumentException("消息不能为空")
            );
            return emitter;
        }

        Flux<String> content = chatClient.prompt()
                .user(message)
                .toolCallbacks(weatherToolCallback)
                .stream()
                .content();
        content.subscribe(
                chunk -> {
                    try {
                        emitter.send(
                                SseEmitter.event()
                                        .data(chunk)
                                        .build()
                        );
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );

        return emitter;
    }

    @PostMapping(
            value = "/chat3",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat3(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter(120_000L);

        if (message == null || message.isBlank()) {
            emitter.completeWithError(
                    new IllegalArgumentException("消息不能为空")
            );
            return emitter;
        }

        Flux<String> content = chatClient.prompt()
                .user(message)
                .tools(new WeatherToolV3())
                .toolContext(Map.of("userName","三只小猪"))
                .stream()
                .content();
        content.subscribe(
                chunk -> {
                    try {
                        emitter.send(
                                SseEmitter.event()
                                        .data(chunk)
                                        .build()
                        );
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );

        return emitter;
    }

    @PostMapping(
            value = "/chat4",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat4(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter(120_000L);

        if (message == null || message.isBlank()) {
            emitter.completeWithError(
                    new IllegalArgumentException("消息不能为空")
            );
            return emitter;
        }

        Flux<String> content = chatClient
                .prompt(message)
                .toolCallbacks(weatherToolV4)
                .toolContext(Map.of("userId","123"))
                .stream()
                .content();
        content.subscribe(
                chunk -> {
                    try {
                        emitter.send(
                                SseEmitter.event()
                                        .data(chunk)
                                        .build()
                        );
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );

        return emitter;
    }
}
