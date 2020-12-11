package com.forest.dataacquisitionserver.protocol.params;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthParams {
    @SerializedName("client_id")
    private String clientId;
    @SerializedName("keep_alive_interval")
    private int keepAliveInterval;
}
