package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.demo.attendance.dto.ActiveWorkerCacheDto;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, ActiveWorkerCacheDto> activeWorkerRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ActiveWorkerCacheDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ActiveWorkerCacheDto> valueSerializer = new Jackson2JsonRedisSerializer<>(
                ActiveWorkerCacheDto.class);
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
