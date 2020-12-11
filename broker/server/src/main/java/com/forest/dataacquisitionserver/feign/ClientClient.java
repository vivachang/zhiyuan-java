package com.forest.dataacquisitionserver.feign;

import feign.Param;
import feign.RequestLine;

public interface ClientClient {
    @RequestLine("GET /client/kick-local/{clientId}/{sessionId}")
    String kickLocal(@Param("clientId") String clientId, @Param("sessionId") String sessionId);
}