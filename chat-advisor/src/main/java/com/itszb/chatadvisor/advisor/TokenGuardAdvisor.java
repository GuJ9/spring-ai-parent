package com.itszb.chatadvisor.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.client.observation.ChatClientPromptContentObservationHandler;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
public class TokenGuardAdvisor implements CallAdvisor, StreamAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);




    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        return null;
    }

    private static final int MAX_TOKEN = 20;
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        //获取prompt中的token数量
        Prompt originalPrompt = chatClientRequest.prompt();
        List<Message> modiMessage = new ArrayList<>();
        Boolean modified = false;
        for (Message message : originalPrompt.getInstructions()) {
            int estimatedTokens = 0;
            if (message instanceof UserMessage userMsg) {
                String content = userMsg.getText();
                //计算token的长度,中文单词是0.8
                estimatedTokens = (int) (content.length() * 0.8);

                if (estimatedTokens > MAX_TOKEN) {
                    logger.info("提示词过长{}，自动截断值：{}", estimatedTokens, MAX_TOKEN);

                    //截断提示词
                    int truncatedLength = (int) (MAX_TOKEN / 0.8);
                    String truncatedContent = content.substring(0, truncatedLength);
                    //截断后的文本，重新构建uesrMessage
                    UserMessage newUserMsg = UserMessage.builder().text(truncatedContent)
                            .media(userMsg.getMedia())
                            .metadata(userMsg.getMetadata())
                            .build();
                    logger.info("截断后的文本：{}", truncatedContent);
                    modiMessage.add(newUserMsg);
                    modified = true;

                }else {
                    modiMessage.add(message);
                }
            }else{
                //有可能是systemMsg
                modiMessage.add(message);
            }
        }
            //重建ChatClientRequest
        ChatClientRequest modifiedRequest = chatClientRequest;
        if (modified){
            Prompt prompt = new Prompt(modiMessage);
            modifiedRequest = new ChatClientRequest(prompt, chatClientRequest.context());
        }
        return  streamAdvisorChain.nextStream(modifiedRequest);
        }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE + 1;
    }
}
