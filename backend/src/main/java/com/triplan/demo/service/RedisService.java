package com.triplan.demo.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Зберегти значення в Redis
     */
    public void save(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Зберегти значення з TTL (time to live)
     */
    public void save(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Отримати значення з Redis
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Видалити значення з Redis
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Перевірити чи існує ключ
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Встановити час життя для ключа
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * Інкремент значення
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * Декремент значення
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }
}