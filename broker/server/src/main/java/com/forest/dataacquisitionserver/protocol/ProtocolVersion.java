package com.forest.dataacquisitionserver.protocol;

import com.google.gson.annotations.SerializedName;

public enum ProtocolVersion {
    @SerializedName("1.0")
    V1_0("1.0");

    private final String version;

    private ProtocolVersion(final String version) {
        this.version = version;
    }

    public String getVersionString() {
        return this.version;
    }
}
