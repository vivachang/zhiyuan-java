package com.forest.dataacquisitionserver.protocol;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum ProtocolCode {
    NOT_WELL_FORMED(-32700, "Parse error"),
    UNSUPPORTED_ENCODING(-32701, "Unsupported encoding"),
    TOO_LONG_FRAME(-32702, "Too Long Frame"),
    INVALID_REQUEST(-32600, "Invalid request"),
    METHOD_NOT_FOUND(-32601, "Method not found"),
    INVALID_PARAMS(-32602, "Invalid params"),
    INVALID_TIMESTAMP(-32603, "Invalid timestamp"),
    INTERNAL_ERROR(-32500, "Internal error"),

    CLIENT_NOT_FOUND(-50001, "Client not found"),
    LOGON_OCCUPIED(-50002, "Logon occupied"),
    UNAUTHORIZED(-50003, "Unauthorized");

    private final int code;
    private final String message;

    private ProtocolCode(int code, final String message) {
        this.code = code;
        this.message = message;
    }
}