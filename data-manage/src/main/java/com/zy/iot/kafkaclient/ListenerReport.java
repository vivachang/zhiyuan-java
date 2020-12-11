package com.zy.iot.kafkaclient;

import com.alibaba.fastjson.JSONObject;
import com.zy.iot.datahandle.sevice.ICalcData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * 从kafka接收数据
 */

public class ListenerReport {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ICalcData caclData;

    // kafka先暂时不启用，否则会报错
    @KafkaListener(topics = {"${kafka.topic-name}"})
    public void listen(ConsumerRecord<?, ?> record) throws Exception{
        try{
            String key = record.key().toString();
            String value = record.value().toString();
            JSONObject obj = JSONObject.parseObject(value);
            logger.debug("收到device-report数据：" + obj);
            if("1".equals(obj.getString("type"))){
                caclData.caclData(obj);
            }else{
                caclData.repairCaclData(obj);
            }
        }catch (Exception e){
            logger.error("项目运行报错：" + e.getMessage());
        }
    }
}
