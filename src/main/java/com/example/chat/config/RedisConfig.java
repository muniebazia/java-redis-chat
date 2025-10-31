package com.example.chat.config;

import com.example.chat.ws.RedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

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
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory, RedisSubscriber redisSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(redisSubscriber, new PatternTopic("room:*"));
        return container;
    }

}
