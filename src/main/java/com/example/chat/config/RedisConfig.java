package com.example.chat.config;

import com.example.chat.ws.RedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    // connection
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        return new LettuceConnectionFactory();
    }

    // used in ChatWebSocketHandler.java
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {

        return new StringRedisTemplate(factory);
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {

        // knows what to do when a message is received
        return new MessageListenerAdapter(subscriber, "handleMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory, MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(listenerAdapter, new PatternTopic("room:*"));
        return container;
    }

}
