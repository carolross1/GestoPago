package com.proyecto.servicios.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    @Test
    @DisplayName("redisConnectionFactory y stringRedisTemplate configuran los beans")
    void configuracionRedis() {
        RedisConfig config = new RedisConfig();
        ReflectionTestUtils.setField(config, "host", "localhost");
        ReflectionTestUtils.setField(config, "port", 6379);
        ReflectionTestUtils.setField(config, "connectTimeoutMs", 2000L);
        ReflectionTestUtils.setField(config, "commandTimeoutMs", 2000L);

        LettuceConnectionFactory factory = config.redisConnectionFactory();
        assertNotNull(factory);
        assertEquals("localhost", factory.getHostName());
        assertEquals(6379, factory.getPort());

        StringRedisTemplate template = config.stringRedisTemplate(factory);
        assertNotNull(template);
    }
}
