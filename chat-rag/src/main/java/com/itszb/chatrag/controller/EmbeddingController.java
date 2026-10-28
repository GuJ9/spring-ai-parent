package com.itszb.chatrag.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.itszb.chatrag.service.EmbeddingService;
import com.itszb.chatrag.utils.TravelTextSplitter;
import com.itszb.chatrag.vo.EmbeddingVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/embedding")
public class EmbeddingController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private DashScopeEmbeddingModel dashScopeEmbeddingModel;

    @Autowired
    private ZhiPuAiEmbeddingModel zhiPuAiEmbeddingModel;

    @Autowired
    private EmbeddingService embeddingService;

    @PostMapping(
            value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat(@RequestBody String message) {
        EmbeddingResponse embeddingResponse = dashScopeEmbeddingModel.embedForResponse(List.of(message));
        System.out.println(Arrays.toString(embeddingResponse.getResult().getOutput()));
        SseEmitter emitter = new SseEmitter(120_000L);
        return emitter;
    }

    @PostMapping(
            value = "/chat2",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat2(@RequestBody String message) {
        EmbeddingResponse embeddingResponse = zhiPuAiEmbeddingModel.embedForResponse(List.of(message));
        System.out.println(Arrays.toString(embeddingResponse.getResult().getOutput()));
        SseEmitter emitter = new SseEmitter(120_000L);
        return emitter;
    }


    /**
     * 相似文本查找接口（支持指定算法）
     * @param query 查询文本
     * @param algorithm 相似度算法（默认 COSINE）
     */
    @GetMapping("/similarity")
    public Map<String, Object> findSimilarText(
            @RequestParam("query") String query,
            @RequestParam(value = "algorithm", defaultValue = "COSINE") String algorithm) {
        // 校验算法参数合法性
        EmbeddingService.SimilarityAlgorithm simAlgo;
        try {
            simAlgo = EmbeddingService.SimilarityAlgorithm.valueOf(algorithm.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Map.of(
                    "error", "算法参数无效，支持的算法：COSINE/EUCLIDEAN/MANHATTAN",
                    "code", 400
            );
        }

        // 执行相似文本查找
        String similarText = embeddingService.queryBestMatch(query, simAlgo);
        return Map.of(
                "query", query,
                "algorithm", simAlgo.name(),
                "answer", similarText
        );
    }

    @Autowired
    private  VectorStore vectorStore;
    @GetMapping("/store")
    public void store() {
        Resource resource = new ClassPathResource("/knowledge/travel.text");
        TextReader textReader = new TextReader(resource);
        List<Document> documents = textReader.get();
        documents.forEach(System.out::println);
        TextSplitter textSplitter = new TravelTextSplitter();
        List<Document> documentList = textSplitter.apply(documents);
        documentList.forEach(System.out::println);
        vectorStore.add(documentList);
    }

    @GetMapping("/search")
    public  List<Document> search(@RequestParam("query") String query) {
        SearchRequest searchRequest = SearchRequest
                .builder()
                .query(query)
                .topK(5)
                .similarityThreshold(0.6)// 相似度阈值
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }


    @PostMapping("/chat3")
    public SseEmitter chat3(@RequestBody EmbeddingVo reqVo) {
        SseEmitter emitter = new SseEmitter();

        List<Document> searchResult = search(reqVo.getMessage());
        // 2. 构造提示词模板：组织原问题和检索结果
        PromptTemplate promptTemplate = new PromptTemplate(
                "{question}\n\n 用以下信息回答问题:\n {contents}"
        );

        // 3. 填充模板：将真实值代入占位符
        Prompt prompt = promptTemplate.create(Map.of(
                "question", reqVo.getMessage(),
                "contents", searchResult
        ));

        Flux<String> stream = chatClient
                .prompt(prompt)
                .advisors(advisor->advisor.param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .stream().content();
        stream.subscribe(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk).build());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError, emitter::complete);
        return emitter;
    }
}
