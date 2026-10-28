package com.itszb.chattools.tools;

import com.itszb.chattools.vo.WeatherRequest;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

//编程式 FunctionToolCallback
@Component
public class WeatherToolV2 {

    @Bean
    public FunctionToolCallback<WeatherRequest,String> WeatherToolCallback(){
        return FunctionToolCallback.<WeatherRequest,String>builder("getWeatherV2",
                request -> request.getCityName()+ "的天气是晴天")
                .description("根据城市查询天气")
                .inputType(WeatherRequest.class)
                .build();

    }
}
