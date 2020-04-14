package com.github.xxscloud.demo.spring;

import com.github.xxscloud.demo.json.JsonObject;
import com.github.xxscloud.demo.json.JsonUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

/**
 * @author Cat.
 * Redis 工具类 API与官方命令一致.
 */
public final class RedisUtils implements ApplicationContextAware {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    private StringRedisSerializer stringSerializer = new StringRedisSerializer();
    private JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();


    public RedisUtils() {

    }

    public RedisUtils(final RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private void redisSerializer() {
        this.redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(jdkSerializationRedisSerializer);
        redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);
    }


    private Object get(final String key) {
        redisSerializer();
        return redisTemplate.opsForValue().get(key);
    }

    public String getString(final String key) {
        final Object obj = get(key);
        return obj == null ? null : String.valueOf(obj);
    }

    public Integer getInt(final String key) {
        final Object obj = get(key);
        return obj == null ? null : Integer.valueOf(obj.toString());
    }

    public Integer getInt(final String key, Integer defaultValue) {
        final Object obj = get(key);
        return obj == null ? defaultValue : Integer.valueOf(obj.toString());
    }

    public Long getLong(final String key) {
        final Object obj = get(key);
        return obj == null ? null : Long.valueOf(obj.toString());
    }

    public Long getLong(final String key, Long defaultValue) {
        final Object obj = get(key);
        return obj == null ? defaultValue : Long.valueOf(obj.toString());
    }

    public Boolean getBoolean(final String key) {
        final Object obj = get(key);
        return obj == null ? null : Boolean.valueOf(obj.toString());
    }

    public Boolean getBoolean(final String key, Boolean defaultValue) {
        final Object obj = get(key);
        return obj == null ? defaultValue : Boolean.valueOf(obj.toString());
    }

    public JsonObject getJsonObject(final String key) {
        final Object obj = get(key);
        return obj != null ? JsonUtils.parseObject(obj.toString()) : null;
    }

    public <T> T getJsonObject(final String key, Class<T> clazz) {
        final Object obj = get(key);
        return obj != null ? JsonUtils.parseObject(obj.toString(), clazz) : null;
    }


    public Boolean publish(final String channel, final String message) {
        redisSerializer();
        redisTemplate.convertAndSend(channel, message);
        return true;
    }

    public Boolean publish(final String channel, byte[] message) {
        redisSerializer();
        redisTemplate.convertAndSend(channel, message);
        return true;
    }

    public Boolean publish(final String channel, Object obj) {
        publish(channel, JsonUtils.stringify(obj));
        return true;
    }


    public Boolean exists(final String key) {
        final Object result = redisTemplate.execute((RedisCallback) redisConnection -> redisConnection.exists(key.getBytes()));
        return result == null ? false : Boolean.valueOf(result.toString());
    }

    public Boolean expire(final String key, int time) {
        return redisTemplate.expire(key, (long) time, TimeUnit.SECONDS);
    }


    public Boolean del(final String key) {
        redisSerializer();
        final Boolean result = redisTemplate.delete(key);
        return result == null ? false : result;
    }

    public Boolean incr(final String key, Long defaultValue) {
        redisTemplate.setValueSerializer(new GenericToStringSerializer(Long.class));
        Long result = redisTemplate.opsForValue().increment(key, 1);
        return result != null;
    }

    public Boolean incr(final String key) {
        return incr(key, 1L);
    }

    public Boolean setex(final String key, int time, Object value) {
        redisSerializer();
        if (value instanceof CharSequence) {
            value = value.toString();
        } else {
            value = JsonUtils.stringify(value);
        }
        redisTemplate.opsForValue().set(key, value, (long) time, TimeUnit.SECONDS);
        return true;
    }

    public Boolean setex(final String key, final String time, final String value) {
        redisSerializer();
        redisTemplate.opsForValue().set(key, value, Long.valueOf(time), TimeUnit.SECONDS);
        return true;
    }


    public Boolean setObject(final String key, int time, Object obj) {
        redisSerializer();
        redisTemplate.opsForValue().set(key, JsonUtils.stringify(obj), (long) time, TimeUnit.SECONDS);
        return true;
    }


    public Boolean leftPush(final String key, final String value) {
        redisSerializer();
        redisTemplate.opsForList().leftPush(key, value);
        return true;
    }


    public Object rpop(final String key) {
        return redisTemplate.opsForList().rightPop(key);
    }


    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {


    }
}

