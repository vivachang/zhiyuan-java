package com.forest.dataacquisitionserver.protocol.params;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatteryParams {
    @SerializedName("battery")
    private Integer battery;
    private String clientId;
    private String event;
}
