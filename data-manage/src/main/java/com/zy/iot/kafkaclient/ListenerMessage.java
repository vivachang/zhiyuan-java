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
            if("1".equals(receive.getString("event"))){
                // 电量
                JSONObject object = JSONObject.parseObject(tagsVal);
                object.remove("status");
                object.remove("updateTime");
                object.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
                object.put("event","1");
                if(receive.getString("battery") != null){
                    object.put("battery",receive.getString("battery"));
                    redisCache.pushList("javasay",object);
                }

                // 经纬度
                if(receive.getString("coordinate") != null && !"".equals(receive.getString("coordinate"))){
                    JSONObject object2 = JSONObject.parseObject(tagsVal);
                    object2.remove("status");
                    object2.remove("updateTime");
                    object2.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
                    object2.put("event","5");
                    object2.put("coordinate",receive.getString("coordinate"));
                    redisCache.pushList("javasay",object2);
                }

                // 信号
                if(receive.getString("signal") != null && !"".equals(receive.getString("signal"))){
                    JSONObject object3 = JSONObject.parseObject(tagsVal);
                    object3.remove("status");
                    object3.remove("updateTime");
                    object3.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
                    object3.put("event","7");
                    object3.put("signal",receive.getString("signal"));
                    redisCache.pushList("javasay",object3);
                }

                // 状态
                if(receive.getString("status") != null && !"".equals(receive.getString("status"))){
                    JSONObject object4 = JSONObject.parseObject(tagsVal);
                    object4.remove("status");
                    object4.remove("updateTime");
                    object4.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
                    if("2".equals(receive.getString("status"))){
                        object4.put("event","9");
                    }else{
                        object4.put("event","8");
                    }
                    redisCache.pushList("javasay",object4);
                }
            }else if("4".equals(receive.getString("event"))){
                JSONObject object = JSONObject.parseObject(tagsVal);
                object.remove("status");
                object.remove("updateTime");
                object.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
                object.put("event","4");
                logger.debug("发送硬件离线数据：" +object);
                redisCache.pushList("javasay",object);
            }else if("6".equals(receive.getString("event"))){
                JSONObject object = JSONObject.parseObject(tagsVal);
                object.remove("status");
                object.remove("updateTime");
                object.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
                object.put("event","6");
                logger.debug("发送硬件上线数据：" +object);
                redisCache.pushList("javasay",object);
            }else{
                logger.debug("错误的device-message信息");
            }

        }catch (Exception e){
            logger.error("项目运行报错：" + e.getMessage());
        }
    }
}
