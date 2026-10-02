package com.westlakers.leap_bff.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.westlakers.leap_bff.dtos.PriceDTO;

/**
 * Redis configuration for live price streaming.
 * Sets up custom RedisTemplate for PriceDTO serialization.
 */
@Configuration
public class RedisConfig {
    
    /**
     * Configure RedisTemplate for PriceDTO serialization.
     * Uses JSON serialization for values to maintain type information.
     */
    @Bean
    @ConditionalOnMissingBean(name = "priceRedisTemplate")
    public RedisTemplate<String, PriceDTO> priceRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, PriceDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serialization for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Use JSON serialization for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
