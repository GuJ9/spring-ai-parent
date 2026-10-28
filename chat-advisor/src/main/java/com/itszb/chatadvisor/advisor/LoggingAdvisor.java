package com.itszb.chatadvisor.advisor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class LoggingAdvisor implements CallAdvisor, StreamAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);

    @NotNull
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 1. 前置处理：记录请求信息
        String model = chatClientRequest.prompt().getOptions() != null
                ? chatClientRequest.prompt().getOptions().getModel()
                : "unknown";
        logger.info("【AI 请求】模型: {}, Prompt: {}", model, chatClientRequest.prompt().getContents());

        try {  // 2. 调用下一个Advisor（核心：必须调用，否则请求到不了大模型）
            ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);
            ChatResponse chatResponse = response.chatResponse();
            assert chatResponse != null;
            String content = chatResponse.getResult().getOutput().getText();

            if (content == null) {
                content = "";
            }
            // 3. 后置处理：记录响应信息
            logger.info("【AI 响应】Token 消耗: {}, 内容: {}",
                    chatResponse.getMetadata().getUsage().getTotalTokens(),
                    content);
            // 4. 返回响应（可以修改后返回，这里直接返回）
            return response;
        } catch (Exception e) {
            // 5. 异常处理：记录调用失败的日志
            logger.error("【AI调用异常】请求处理失败，提示词：{}，异常信息：{}",
                    chatClientRequest.prompt(), e.getMessage(), e);
            throw e; // 抛出异常，让上层处理
        }

    }

    @NotNull
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        // 1. 前置处理：记录流式请求信息
        logger.info("【AI流式调用请求】提示词内容：{}", chatClientRequest.prompt());

        // 2. 调用下一个Advisor，获取流式响应
        Flux<ChatClientResponse> responseFlux = streamAdvisorChain.nextStream(chatClientRequest);


        // 3. 处理每一段流式响应（逐段记录）
        return responseFlux
                .doOnNext(response -> {
                    // 每收到一段响应，就记录
                    assert response.chatResponse() != null;
                    String content = response.chatResponse().getResult().getOutput().getText();
                    logger.info("【AI流式响应片段】内容：{}", content);
                })
                .doOnComplete(() -> {
                    // 流式响应结束，记录完成日志
                    logger.info("【AI流式调用完成】提示词：{}", chatClientRequest.prompt());
                })
                .doOnError(e -> {
                    // 流式调用异常，记录错误
                    logger.error("【AI流式调用异常】提示词：{}，异常：{}",
                            chatClientRequest.prompt(), e.getMessage(), e);
                });

    }

    /**
     * 给Advisor起个名字，方便后续排查问题/监控
     *
     * @return Advisor名称
     */
    @NotNull
    @Override
    public String getName() {
        // 直接用类名，清晰好认
        return this.getClass().getSimpleName();
    }


    /**
     * 设置执行顺序：0是中等优先级，数字越小越先执行
     * 日志建议放中等优先级，不影响核心逻辑
     *
     * @return 执行顺序数值
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100; // 比最低优先级高一点，最后处理请求，最先处理响应
    }
}