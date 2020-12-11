package com.forest.dataacquisitionserver.protocol;

import com.google.gson.annotations.SerializedName;

public enum PacketType {
    @SerializedName("auth")
    AUTH("auth"),
    @SerializedName("ping")
    PING("ping"),
    @SerializedName("pong")
    PONG("pong"),
    @SerializedName("report")
    REPORT("report"),
    @SerializedName("time")
    TIME("time"),
    @SerializedName("repairReport")
    REPAIRREPORT("repairReport"),
    @SerializedName("battery")
    BATTERY("battery");

    private final String method;

    private PacketType(final String method) {
        this.method = method;
    }

    public String getTypeString() {
        return method;
    }
}
