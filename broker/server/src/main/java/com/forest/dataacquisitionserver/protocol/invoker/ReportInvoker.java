package com.forest.dataacquisitionserver.protocol.invoker;

import com.forest.dataacquisitionserver.netty.NettyAttributeKey;
import com.forest.dataacquisitionserver.protocol.*;
import com.forest.dataacquisitionserver.protocol.params.ReportParams;
import com.forest.dataacquisitionserver.session.Session;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * 数据上报处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class ReportInvoker implements MethodInvoker {
    private final Gson gson;
    private final ProtocolUtil protocolUtil;
    private final KafkaTemplate kafkaTemplate;
    private final ProtocolProperty protocolProperty;
    @Value("${kafka.topic-name}")
    private String topicName;

    @Override
    public void call(ChannelHandlerContext ctx, JsonElement id, JsonElement params) {
        Attribute<Session> sessionAttr = ctx.channel().attr(NettyAttributeKey.SESSION);
        Session session = sessionAttr.get();
        // 判断参数是否为空
        if (null == session) {
            protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.UNAUTHORIZED);
        }

        if (null == id || null == params || !params.isJsonObject()) {
            protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.INVALID_REQUEST);
        }
        log.debug("receive data " + params.toString());
        boolean result = false;
        boolean timestampIsOk = true;
        try {
            // 数据对象化
            ReportParams reportParams = gson.fromJson(params, ReportParams.class);
            if (null != reportParams) {
                // 此处为判断数据时间合法性，因为存在网络不好的情况，此处暂时屏蔽
                long currentTimestamp = System.currentTimeMillis();
                long reportTimestamp = reportParams.getTimestamp();
                long forwardOffset = protocolProperty.getReportTimestampForwardOffset();
                long backwardOffset = protocolProperty.getReportTimestampBackwardOffset();
                // 判断时间是否在规定时间内
//                if (forwardOffset >= 0 && reportTimestamp >= currentTimestamp + forwardOffset * 1000) {
//                    log.info("invalid timestamp, current:{}, forward offset:{}, current + offset:{}, report timestamp:{}",
//                            currentTimestamp, forwardOffset, currentTimestamp + forwardOffset * 1000, reportTimestamp);
//                    timestampIsOk = false;
//                    protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.INVALID_TIMESTAMP);
//                }
//                // 判断时间是否在规定时间内
//                if (backwardOffset >= 0 && reportTimestamp <= currentTimestamp - backwardOffset * 1000) {
//                    log.info("invalid timestamp, current:{}, backward offset:{}, current - offset:{}, report timestamp:{}",
//                            currentTimestamp, forwardOffset, currentTimestamp + forwardOffset * 1000, reportTimestamp);
//                    timestampIsOk = false;
//                    protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.INVALID_TIMESTAMP);
//                }

                assert session != null;
                reportParams.setClientId(session.getClientId());
                reportParams.setType("1"); //实时数据
                String message = gson.toJson(reportParams, ReportParams.class);
                ListenableFuture<SendResult> future = kafkaTemplate.send(topicName, session.getClientId(), message);
                SendResult sendResult = future.get();
                log.debug("producer send ok " + sendResult.getProducerRecord());
                result = true;
            } else {
                protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.INVALID_PARAMS);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            result = false;
        }
        // 返回信息给终端
        ResponsePacket responsePacket;
        if (!timestampIsOk) {
            responsePacket = protocolUtil.buildErrorResponse(id, ProtocolCode.INVALID_TIMESTAMP);
        } else {
            if (result) {
                responsePacket = protocolUtil.buildResponse(id, new JsonPrimitive("ok"));
            } else {
                responsePacket = protocolUtil.buildResponse(id, new JsonPrimitive("fail"));
            }
        }

        ctx.channel().writeAndFlush(gson.toJson(responsePacket, ResponsePacket.class));
    }
}
