package com.forest.dataacquisitionserver.protocol;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwitchPacket {
    @SerializedName("event")
    private String event;
    @SerializedName("deviceId")
    private String deviceId;
    @SerializedName("timestamp")
    private String timestamp;
}
