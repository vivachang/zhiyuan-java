package com.forest.dataacquisitionserver.util;

import com.forest.dataacquisitionserver.dto.Message;
import com.forest.dataacquisitionserver.protocol.params.ReportParams;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageUtil {
    private final Gson gson;
    private final KafkaTemplate kafkaTemplate;
    @Value("${kafka.topic-message-name}")
    private String topicMessageName;

    public Message ofOnlineMessage(final String clientId) {
        Message message = new Message();
        message.setClientId(clientId);
        message.setEvent("6");
        message.setTimestamp(String.valueOf(System.currentTimeMillis()));
        return message;
    }

    public Message ofOfflineMessage(final String clientId) {
        Message message = new Message();
        message.setClientId(clientId);
        message.setEvent("4");
        message.setTimestamp(String.valueOf(System.currentTimeMillis()));
        return message;
    }

    public void send(final Message message) throws ExecutionException, InterruptedException {
        String msg = gson.toJson(message, Message.class);
        ListenableFuture future = kafkaTemplate.send(topicMessageName,
                message.getClientId(), msg);
        SendResult sendResult = (SendResult) future.get();
        log.debug("producer send ok " + sendResult.getProducerRecord());
    }
}