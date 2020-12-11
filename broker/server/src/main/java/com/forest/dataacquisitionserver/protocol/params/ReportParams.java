package com.forest.dataacquisitionserver.protocol.params;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportParams {
    @SerializedName("air_info")
    private JsonElement values;
    @SerializedName("timestamp")
    private Long timestamp;
    private String clientId;
    private String type;
}