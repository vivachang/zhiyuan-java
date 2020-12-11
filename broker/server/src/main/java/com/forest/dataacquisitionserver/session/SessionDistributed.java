package com.forest.dataacquisitionserver.session;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SessionDistributed {
    private String sessionId;
    private String clientId;
    private String nodeId;
    private String nodeUri;
    private String remoteHost;
    private int remotePort;
    private int expire;
    @Builder.Default
    private long logonTime = System.currentTimeMillis();
}