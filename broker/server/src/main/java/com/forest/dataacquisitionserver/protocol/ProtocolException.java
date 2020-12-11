package com.forest.dataacquisitionserver.protocol;

import com.google.gson.JsonElement;

public class ProtocolException extends RuntimeException {
    protected final ProtocolCode protocolCode;
    protected final JsonElement id;

    public ProtocolException(JsonElement id, ProtocolCode protocolCode) {
        super(protocolCode.getMessage());
        this.protocolCode = protocolCode;
        this.id = id;
    }

    public ProtocolException(ProtocolCode protocolCode) {
        super(protocolCode.getMessage());
        this.protocolCode = protocolCode;
        this.id = null;
    }

    public ProtocolCode getProtocolCode() {
        return this.protocolCode;
    }

    public JsonElement getId() {
        return this.id;
    }
}
