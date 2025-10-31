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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentMap<String, CopyOnWriteArraySet<WebSocketSession>> roomPeers = new ConcurrentHashMap<>();

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

        roomPeers.computeIfAbsent(room, k -> new CopyOnWriteArraySet<>()).add(session);

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

        redis.convertAndSend("room: " + room, conversion);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // when someone leaves
        String room = (String) session.getAttributes().get("room");
        String user = (String) session.getAttributes().get("user");

        roomPeers.getOrDefault(room, new CopyOnWriteArraySet<>()).remove(session);
        publish(room, user, user + " left");
    }

    // helper functions

    private Map<String, String> parseURL(URI uri) {

        Map<String, String> map = new HashMap<>() {};
        if (uri == null) {
            return map;
        }

        String qs = uri.getQuery();
        for (String kv : qs.split("&")) {
            String[] p = kv.split("=", 2);
            if (p.length == 2) {
                map.put(urlDecode(p[0]), urlDecode(p[1]));
            }
        }
        return map;
    }

    private String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private void publish(String room, String user, String s) throws Exception {

        ChatMessage message = new ChatMessage(room, user, s);
        String payload = mapper.writeValueAsString(message);
        redis.convertAndSend("room:" + room, payload);
    }

    public void broadcast(String room, String message) {

        // Redis gives the message to RedisSubscriber to send to the servers
        // this will send it to the servers (called by RedisSubscriber)
        var peers = roomPeers.getOrDefault(room, new CopyOnWriteArraySet<>());
        var dead = new ArrayList<WebSocketSession>();
        for (var s : peers) {
            try {
                s.sendMessage(new org.springframework.web.socket.TextMessage(message));
            } catch (Exception e) {
                dead.add(s); // socket is closed/broken
            }
        }
        peers.removeAll(dead);
    }
}