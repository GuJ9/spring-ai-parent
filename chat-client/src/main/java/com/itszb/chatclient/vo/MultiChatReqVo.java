package com.itszb.chatclient.vo;

import lombok.Data;

@Data
public class MultiChatReqVo {
    private String platform;
    private String model;
    private String message;
}
