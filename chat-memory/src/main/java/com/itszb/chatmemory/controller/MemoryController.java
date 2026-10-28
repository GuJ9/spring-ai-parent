
package com.itszb.chatmemory.controller;

import com.itszb.chatmemory.vo.MemoryReqVo;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
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
@RequestMapping("/memory")
@RequiredArgsConstructor
public class MemoryController {
    private final ChatClient chatClient;

    @Resource(name = "chatClient")
    private ChatClient ChatClient;

    @Resource(name = "jdbcChatClient")
    private ChatClient jdbcChatClient;

    @Resource(name = "redisChatClient")
    private ChatClient redisChatClient;

    @PostMapping(
            value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat(@RequestBody String message) {
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

    @PostMapping(
            value = "/chat4",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat4(@RequestBody MemoryReqVo reqVo) {
        SseEmitter emitter = new SseEmitter(120_000L);

        String conversationId = reqVo.getConversationId();

        if (conversationId == null || conversationId.isBlank()) {
            emitter.completeWithError(
                    new IllegalArgumentException("消息不能为空")
            );
            return emitter;
        }

        Flux<String> content = redisChatClient.prompt()
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        conversationId
                ))
                .user(reqVo.getMessage())
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
