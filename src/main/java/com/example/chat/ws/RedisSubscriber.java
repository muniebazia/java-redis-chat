package com.example.chat.ws;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class RedisSubscriber implements MessageListener {

    private final ChatWebSocketHandler handler;

    public RedisSubscriber(ChatWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        String room = channel.startsWith("room:") ? channel.substring(5) : channel;
        System.out.println(room);
        handler.broadcast(room, body);
    }
}
