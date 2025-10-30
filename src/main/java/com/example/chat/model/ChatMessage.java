package com.example.chat.model;

import lombok.Builder;

@Builder
public class ChatMessage {
    private String room;
    private String user;
    private String text;
}
