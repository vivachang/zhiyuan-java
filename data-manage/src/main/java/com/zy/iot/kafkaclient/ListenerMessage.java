package com.zy.iot.kafkaclient;

import com.alibaba.fastjson.JSONObject;
import com.zy.iot.cache.RedisCache;
import com.zy.iot.config.Constant;
import com.zy.iot.utils.DateUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Date;

/**
 * @author AnGuangYing
 * @since 2019-04-15 11:38
 */

public class ListenerMessage {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisCache redisCache;
    @Autowired
    Constant constant;

    @KafkaListener(topics = {"${kafka.topic-message-name}"})
    public void listen(ConsumerRecord<?, ?> record) throws Exception{
        try{
            //这里把数据写入txt
            String value = record.value().toString();
            JSONObject receive = JSONObject.parseObject(value);
            logger.debug("收到device-message数据：" +receive);
            String tagsVal = redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TAGS,receive.getString("clientId"));
            if (null==tagsVal){
                return;
            }
            JSONObject object = JSONObject.parseObject(tagsVal);
            object.remove("status");
            object.remove("updateTime");
            object.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
            object.put("event",receive.getString("event"));
            if(receive.getString("battery") != null){
                object.put("battery",receive.getString("battery"));
            }
            redisCache.pushList("javasay",object);
        }catch (Exception e){
            logger.error("项目运行报错：" + e.getMessage());
        }
    }
}
