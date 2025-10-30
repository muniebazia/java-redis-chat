package com.example.chat.ws;

import org.springframework.stereotype.Component;

@Component
public class RedisSubscriber {
    private final ChatWebSocketHandler handler;

    public RedisSubscriber(ChatWebSocketHandler handler) {
        this.handler = handler;
    }

    public void handleMessage(String message, String channel) {
        String room = channel.substring("room:".length());
        handler.broadcast(room, message);
    }

}
