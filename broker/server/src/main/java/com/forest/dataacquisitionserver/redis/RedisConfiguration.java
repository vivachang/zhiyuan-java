package com.forest.dataacquisitionserver.redis;

import com.forest.dataacquisitionserver.protocol.invoker.OnOffOutvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisConfiguration {
    private RedisConnectionFactory factory;

    @Bean
    public RedisScript<String> getAndRemoveScript() {
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<String>();
        ScriptSource scriptSource = new ResourceScriptSource(
                new ClassPathResource("/META-INF/scripts/get_and_remove.lua"));
        redisScript.setScriptSource(scriptSource);
        redisScript.setResultType(String.class);
        return redisScript;
    }

    @Bean
    public RedisScript<Boolean> removeIfScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<Boolean>();
        ScriptSource scriptSource = new ResourceScriptSource(
                new ClassPathResource("/META-INF/scripts/remove_if.lua"));
        redisScript.setScriptSource(scriptSource);
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }

    @Bean
    public RedisScript<Boolean> setAndExpireScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<Boolean>();
        ScriptSource scriptSource = new ResourceScriptSource(
                new ClassPathResource("/META-INF/scripts/set_and_expire.lua"));
        redisScript.setScriptSource(scriptSource);
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }

    @Bean
    public RedisScript<String> getAndExpireScript() {
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<String>();
        ScriptSource scriptSource = new ResourceScriptSource(
                new ClassPathResource("/META-INF/scripts/get_and_expire.lua"));
        redisScript.setScriptSource(scriptSource);
        redisScript.setResultType(String.class);
        return redisScript;
    }

    @Autowired
    public RedisConfiguration(RedisConnectionFactory factory) {
        this.factory = factory;
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate() {
        final StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        return template;
    }
}
