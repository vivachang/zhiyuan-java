package com.zy.iot.mqtt; /**
 *
 * Description:
 * @author admin
 * 2017年2月10日下午18:04:07
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zy.iot.datahandle.sevice.impl.CalcData;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息的回调类
 *
 */
@Component
public class MessageHandle implements MqttCallback {

    private static int count = 1;

    @Autowired
    CalcData calcData;

    @Override
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
        System.out.println("连接回调");
    }

    @Override
    public void mqttErrorOccurred(MqttException e) {
        System.out.println("连接回调1");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe后得到的消息会执行到这里面
        //System.out.println("接收消息时间 : " + System.currentTimeMillis());
        //System.out.println("接收消息主题 : " + topic);
        //System.out.println("接收消息Qos : " + message.getQos());
        //System.out.println("接收消息内容 : " + new String(message.getPayload()));
        System.out.println("收到数据");
        JSONObject jsonObject = JSON.parseObject(new String(message.getPayload()));
        calcData.caclData(jsonObject);
    }

    @Override
    public void deliveryComplete(IMqttToken iMqttToken) {
        System.out.println("连接回调2");
    }

    @Override
    public void connectComplete(boolean b, String s) {

    }

    @Override
    public void authPacketArrived(int i, MqttProperties mqttProperties) {
        System.out.println("接收消息Qos : " + mqttProperties.toString());
    }
}