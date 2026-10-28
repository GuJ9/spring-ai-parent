package com.itszb.quickstart;

import com.alibaba.cloud.ai.dashscope.audio.transcription.DashScopeAudioTranscriptionPrompt;
import com.alibaba.cloud.ai.dashscope.audio.transcription.DashScopeAsrTranscriptionApiSpec.DashScopeAudioAsrTranscriptionResponse;
import java.util.List;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAudioSpeechApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAudioTranscriptionApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.audio.tts.DashScopeAudioSpeechModel;
import com.alibaba.cloud.ai.dashscope.audio.tts.DashScopeAudioSpeechOptions;
import com.alibaba.cloud.ai.dashscope.audio.transcription.DashScopeAudioTranscriptionModel;
import com.alibaba.cloud.ai.dashscope.audio.transcription.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.itszb.quickstart.config.DashScopeConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;

import static org.apache.coyote.http11.Constants.a;

@SpringBootTest
public class DashScopeTest {
    @Autowired
    private DashScopeConfig config;

    @Test
    void chat(){
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(config.getApiKey()).build();
        DashScopeChatOptions dashScopeChatOptions = DashScopeChatOptions.builder().temperature(0.7).model(DashScopeApi.DEFAULT_CHAT_MODEL).build();
        DashScopeChatModel dashScopeChatModel = DashScopeChatModel.builder().dashScopeApi(dashScopeApi).defaultOptions(dashScopeChatOptions).build();
        String content = dashScopeChatModel.call("你是谁");
        System.out.println(content);

    }
    @Test
    void ImageTest(){
        DashScopeImageOptions dashScopeImageOptions = DashScopeImageOptions
                .builder()
                .model("wanx-v1")
                .n(1)
                .build();
        DashScopeImageApi dashScopeImageApi = DashScopeImageApi.builder()
                .apiKey(config.getApiKey())
                .build();
        DashScopeImageModel dashScopeImageModel = DashScopeImageModel
                .builder()
                .dashScopeApi(dashScopeImageApi)
                .defaultOptions(dashScopeImageOptions)
                .build();
        ImagePrompt imagePrompt = new ImagePrompt("给我生成一张猫咪的图片");
        ImageResponse response = dashScopeImageModel.call(imagePrompt);
        String url = response.getResult().getOutput().getUrl();
        System.out.println(url);
    }
    @Test
    void TextToAudio(){
        DashScopeAudioSpeechOptions speechOptions = DashScopeAudioSpeechOptions.builder()
                .model(DashScopeModel.AudioModel.COSYVOICE_V3_FLASH.getValue())
                .voice("longhua")
                .speed(1.0)
                .format("mp3")
                .build();
        DashScopeAudioSpeechApi dashScopeAudioSpeechApi =
                DashScopeAudioSpeechApi.builder()
                        .apiKey(new SimpleApiKey(config.getApiKey()))
                        .build();
        DashScopeAudioSpeechModel audioSpeechModel = DashScopeAudioSpeechModel
                .builder().audioSpeechApi(dashScopeAudioSpeechApi)
                .defaultOptions(speechOptions).build();
        TextToSpeechPrompt prompt = new TextToSpeechPrompt("生成一段天气预报");
        //写入本地文件
        File file = new File(System.getProperty("user.dir") + "/output.mp3");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            for (TextToSpeechResponse response : audioSpeechModel.stream(prompt).toIterable()) {
                byte[] output = response.getResult().getOutput();
                outputStream.write(output);
            }
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    void AudioToTest() throws MalformedURLException {
        DashScopeAudioTranscriptionApi transcriptionApi =
                DashScopeAudioTranscriptionApi.builder()
                        .apiKey(new SimpleApiKey(config.getApiKey()))
                        .build();

        DashScopeAudioTranscriptionOptions transcriptionOptions = DashScopeAudioTranscriptionOptions.builder()
                .model(DashScopeModel.AudioModel.PARAFORMER_V2.getValue())
                .build();
        DashScopeAudioTranscriptionModel transcriptionModel = DashScopeAudioTranscriptionModel
                .builder()
                .audioTranscriptionApi(transcriptionApi)
                .defaultOptions(transcriptionOptions).build();
        //String path = System.getProperty("user.dir") + "/output.mp3";

        // 使用有效、可公开访问的音频文件直链
        String audioUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav";

        AudioTranscriptionPrompt audioTranscriptionPrompt =
                new DashScopeAudioTranscriptionPrompt(
                        transcriptionOptions,
                        List.of(audioUrl)
                );

        DashScopeAudioAsrTranscriptionResponse response =
                (DashScopeAudioAsrTranscriptionResponse)
                        transcriptionModel.call(audioTranscriptionPrompt);

        // 遍历并输出语音识别结果
        for (var result : response.getTranscriptionResults()) {
            if (result.transcripts() != null) {
                for (var transcript : result.transcripts()) {
                    System.out.println(transcript.getText());
                }
            }
        }

    }
    @Test
    void ImageTest2(){
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(config.getApiKey()).build();

        //读取文件
        ClassPathResource resource = new ClassPathResource("/file/194138.jpg");
        Media media = new Media(MimeType.valueOf(MimeTypeUtils.IMAGE_JPEG_VALUE), resource);

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .model("qwen3.6-plus")
                .multiModel(true)
                .build();
        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi).defaultOptions(chatOptions).build();
        Prompt prompt = Prompt.builder().messages(UserMessage.builder().media(media)
                .text("识别图像中的内容").build()).build();
        ChatResponse chatResponse = chatModel.call(prompt);
        System.out.println(chatResponse.getResult().getOutput().getText());
    }

    @Test
    void TestToVideo() throws NoApiKeyException, InputRequiredException {
        VideoSynthesis synthesis = new VideoSynthesis();
        VideoSynthesisParam param = VideoSynthesisParam.builder()
                .apiKey(config.getApiKey())
                .model("wan2.7-t2v")
                .prompt("一幅史诗级可爱的场景。一只小巧可爱的卡通小猫将军，身穿细节精致的金色盔甲，头戴一个稍大的头盔，勇敢地站在悬崖上。他骑着一匹虽小但英勇的战马，说：”青海长云暗雪山，孤城遥望玉门关。黄沙百战穿金甲，不破楼兰终不还。“。悬崖下方，一支由老鼠组成的、数量庞大、无穷无尽的军队正带着临时制作的武器向前冲锋。这是一个戏剧性的、大规模的战斗场景，灵感来自中国古代的战争史诗。远处的雪山上空，天空乌云密布。整体氛围是可爱与霸气的搞笑和史诗般的融合。")
                .duration(15)
                .size("1280*720")
                .shotType("multi")
                .promptExtend(true)
                .watermark(false)
                .build();
        System.out.println("please.......wait");
        VideoSynthesisResult result = synthesis.call(param);
        System.out.println(JsonUtils.toJson(result));


    }
}
