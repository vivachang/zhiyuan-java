package com.forest.dataacquisitionserver.session;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Session {
    private String clientId;
    private Channel channel;
    private String sessionDistributedHash;
    private int expire;
}