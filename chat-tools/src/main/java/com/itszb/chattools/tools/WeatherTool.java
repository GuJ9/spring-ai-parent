package com.itszb.chattools.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WeatherTool {
    @Tool(description = "根据城市查询天气")
    public String getWeather(@ToolParam(description = "城市名称") String city){
        return city + "天气晴朗";
    }
}
