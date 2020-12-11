package com.zy.iot.mqtt;

import com.alibaba.fastjson.JSONObject;
import com.zy.iot.cache.RedisCache;
import com.zy.iot.config.Constant;
import com.zy.iot.datahandle.sevice.impl.TsdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * 终端回复消息处理
 */
@Order(1)
@Component
public class ReceiveMsg implements CommandLineRunner {

    @Autowired
    private MqttReceive mqttReceive;
    @Autowired
    RedisCache redisCache;
    @Autowired
    Constant constant;
    @Autowired
    private TsdbService tsdbService;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始启动");
//        mqttReceive.start();
//        for(int i=1;i<=1;i++){
//            JSONObject obj = new JSONObject();
//            String deviceId = "0000001a1b1c";
//            obj.put("deviceId", deviceId);
//            obj.put("monitorId", "1");
//            obj.put("projectId", "3");
//            redisCache.setHashMapfiled("zhiyuanv2:air:devices:tags", deviceId, obj.toJSONString());
//            redisCache.setHashMapfiled("zhiyuanv2:iot:auth:client", deviceId, String.valueOf(1));
//        }
        tsdbService.init();
    }
}
