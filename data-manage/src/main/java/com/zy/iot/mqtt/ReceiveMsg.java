package com.zy.iot.mqtt;

import com.alibaba.fastjson.JSONObject;
import com.zy.iot.cache.RedisCache;
import com.zy.iot.config.Constant;
import com.zy.iot.datahandle.sevice.impl.TsdbService;
import com.zy.iot.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;


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
    }
}
