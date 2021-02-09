package com.forest.dataacquisitionserver.protocol.params;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatteryParams {
    @SerializedName("battery")
    private Integer battery;
    @SerializedName("coordinate")
    private String coordinate;
    @SerializedName("signal")
    private Integer signal;
    @SerializedName("status")
    private Integer status;
    private String clientId;
    private String event;
}
