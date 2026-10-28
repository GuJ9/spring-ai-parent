package com.itszb.chattools.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

//编程式 FunctionToolCallback
@Component
public class WeatherToolV3 {

    @Tool(description = "根据城市查询天气")
    public String getWeatherV3(@ToolParam(description = "城市名称") String city, ToolContext toolContext) {
        System.out.println("用户的信息：" + toolContext.getContext().get("userName"));
        return city + "天气晴朗";
    }
}
