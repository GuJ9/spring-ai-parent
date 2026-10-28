
package com.itszb.chatadvisor.controller;

import com.itszb.chatadvisor.advisor.LoggingAdvisor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/advisor")
@RequiredArgsConstructor
public class AdvisorController {

    private final ChatClient chatClient;

    @PostMapping(
            value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat(@RequestBody Map<String, String> request) {

        String message = request.get("message");

        SseEmitter emitter = new SseEmitter(120_000L);

        if (message == null || message.isBlank()) {
            emitter.completeWithError(
                    new IllegalArgumentException("消息不能为空")
            );
            return emitter;
        }

        chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .subscribe(
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
