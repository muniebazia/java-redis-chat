package com.example.chat.ws;

import com.example.chat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final StringRedisTemplate redis;

    private final ObjectMapper mapper = new ObjectMapper();

    public ChatWebSocketHandler(StringRedisTemplate redis) {

        this.redis = redis;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // when someone connects
        Map<String, String> intel = parseURL(session.getUri());
        String room = intel.getOrDefault("room", "general");
        String user = intel.getOrDefault("user", "anonymous");

        // storing their info for later usage
        session.getAttributes().put("room", room);
        session.getAttributes().put("user", user);

        publish(room, user, user + " joined");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // going to use to create a ChatMessage object to give to Redis
        String room = (String) session.getAttributes().get("room");
        String user = (String) session.getAttributes().get("user");
        String text = message.getPayload();

        ChatMessage chatMessage = ChatMessage.builder().room(room).user(user).text(text).build();

        // convert to JSON and give to Redis
        String conversion = mapper.writeValueAsString(chatMessage);

        redis.convertAndSend(room, conversion);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // when someone leaves
        String room = (String) session.getAttributes().get("room");
        String user = (String) session.getAttributes().get("user");

        publish(room, user, user + " left");
    }

    // helper functions

    private Map<String, String> parseURL(URI uri) {

        // return a map of the info attained from the URL
        return Map.of();
    }

    private ChatMessage system(String room, String s) {

        // return a system message
        return null;
    }

    private void publish(String room, String user, String s) throws Exception {

        // need to turn the message into JSON so that we can send it to redis and
        // redis can send it to the servers
    }

}