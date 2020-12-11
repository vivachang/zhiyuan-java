package com.zy.iot.cache;


import com.alibaba.fastjson.parser.ParserConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis配置类
 */

@Configuration
@EnableCaching
public class RedisCacheConfig extends CachingConfigurerSupport {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.database}")
    private int database;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.pool.maxTotal}")
    private int maxTotal;

    /**
     * 连接redis的工厂类
     * @return
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host,port);
        standaloneConfig.setDatabase(database);
        //standaloneConfig.setPassword(password);
        JedisConnectionFactory factory = new JedisConnectionFactory(standaloneConfig);
        //standaloneConfig类是无法验证Connection是否已失效，只能用Pool模式
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //此数值必须不能大于redis config 中的timeout
        jedisPoolConfig.setMinEvictableIdleTimeMillis(5000);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(2000);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setBlockWhenExhausted(false);
        //生产中服务中不要用设置此参数对性能有很大消耗
        //jedisPoolConfig.setTestOnBorrow(true);
        factory.setPoolConfig(jedisPoolConfig);
        factory.setUsePool(true);
        return factory;
    }

    /**
     * 配置RedisTemplate
     * 设置添加序列化器,配置使用fastjson
     * key 使用string序列化器
     * value 使用Json序列化器
     * 还有一种简答的设置方式，改变defaultSerializer对象的实现。
     *
     * @return
     */
    @Bean
    public RedisSerializer fastJson2JsonRedisSerializer() {
        return new FastJson2JsonRedisSerializer<Object>(Object.class);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, RedisSerializer fastJson2JsonRedisSerializer) {
        RedisTemplate template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        //设置（Default）的序列化
        template.setDefaultSerializer(fastJson2JsonRedisSerializer);
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        // 设置值（value）的序列化采用FastJsonRedisSerializer。
        template.setValueSerializer(fastJson2JsonRedisSerializer);
        template.setHashValueSerializer(fastJson2JsonRedisSerializer);
        // 设置键（key）的序列化采用StringRedisSerializer。
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * RedisSerializationContext.SerializationPair.fromSerializer
     * 此静态方法直接调用在java5 以后是被禁止或不提倡的。目前java8 需要设置为忽略
     * @param fastJson2JsonRedisSerializer
     * @return
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(RedisSerializer fastJson2JsonRedisSerializer) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        configuration = configuration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(fastJson2JsonRedisSerializer));
        return configuration;
    }

}
