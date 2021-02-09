package com.forest.dataacquisitionserver.protocol.invoker;

import com.forest.dataacquisitionserver.protocol.ProtocolUtil;
import com.forest.dataacquisitionserver.protocol.ResponsePacket;
import com.forest.dataacquisitionserver.protocol.SwitchPacket;
import com.forest.dataacquisitionserver.session.Session;
import com.forest.dataacquisitionserver.session.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnOffOutvoker {

    private final Gson gson;
    private final ProtocolUtil protocolUtil;
    private final SessionManager sessionManager;
    private final RedisTemplate<String, String> redisTemplate;

    //这里需要把key提取成常量， 因为存取需要同一个key
    private static final String key = "phpsay";

    //标识符，判断redis里面是否有消息.
    private static  Long keyNum = 0L;



    @Scheduled(cron = "0/10 * * * * ?")
    public void phpMessage(){
        try {
            keyNum = redisTemplate.opsForList().size(key);
            while(keyNum > 0){
                String msg = redisTemplate.opsForList().rightPop(key).toString();
                log.debug("收到开关机数据："+ msg);
                SwitchPacket packet = gson.fromJson(msg, SwitchPacket.class);
                ResponsePacket response = protocolUtil.buildResponse(new JsonPrimitive(7),
                        new JsonPrimitive(Integer.parseInt(packet.getEvent())));
                Session session = sessionManager.get(packet.getDeviceId());
                if (null != session) {
                    Channel channel = session.getChannel();
                    channel.writeAndFlush(gson.toJson(response, ResponsePacket.class));
                }
                //再次获取标识符
                keyNum = redisTemplate.opsForList().size(key);
            }
        } catch (Exception e) {
            log.error("定时器异常信息,{}",e);
        }
    }
}
