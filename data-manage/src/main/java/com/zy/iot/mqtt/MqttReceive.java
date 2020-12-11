package com.zy.iot.mqtt; /**
 *
 * Description:
 * @author admin
 * 2017年2月10日下午17:50:15
 */

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttTopic;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqttReceive {

    public static final String HOST = "tcp://192.168.1.111:1883";
    public static final String TOPIC = "/device/air_test";
    private static final String clientid = "receiveMqttClient";
    private MqttClient client;
    private MqttConnectionOptions options;
    private String userName = "gtjb";
    private String passWord = "gtjb666";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageHandle messageHandle;

    public void start() {
        try {
            // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(HOST, clientid, new MemoryPersistence());
            // MQTT的连接设置
            options = new MqttConnectionOptions();
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(passWord.getBytes());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调
            client.setCallback(messageHandle);

            client.connect(options);
            //订阅消息
            int[] Qos  = {2};
            String[] topic1 = {TOPIC};
            client.subscribe(topic1, Qos);
            System.out.println("启动完成");
        } catch (Exception e) {
            logger.error("项目运行报错：" + e.getMessage());
        }
    }
}
