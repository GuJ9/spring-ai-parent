package com.itszb.chattools.tools;

import com.itszb.chattools.vo.WeatherRequest;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.stereotype.Component;

@Component
public class WeatherToolV4 implements ToolCallback {

    @NotNull
    @Override
    public ToolDefinition getToolDefinition() {
        return ToolDefinition.builder()
                .name("getWeather")
                .description("根据城市查询天气，例如输入北京、上海")
                .inputSchema("""
                {
                  "type": "object",
                          "properties": {
                    "location": {
                      "type": "string",
                      "description": "城市名称，比如北京、上海、东京"
                    }
                  },
                  "required": ["location"]
                }
                """)
                .build();

    }

    @NotNull
    @Override
    public String call(@NotNull String toolInput) {
        return call(toolInput, null);
    }



    /**
     * 执行工具（有 context）
     * @param toolInput
     * @return
     */
    @NotNull
    @Override
    public String call(@NotNull String toolInput, ToolContext toolContext) {
        WeatherRequest weatherRequest = JsonParser.fromJson(toolInput, WeatherRequest.class);
        String cityName = weatherRequest.getCityName();
        String unit ="C";
        String userId = "unknow";
        if(toolContext != null){
            unit = (String)toolContext.getContext().getOrDefault("unit", "C");
            userId = (String)toolContext.getContext().getOrDefault("userId", "unknow");
        }

        String result = "用户" + userId
                + "查询" + toolInput
                + "的天气是晴天";

        System.out.println(result);

        return result;
    }
}