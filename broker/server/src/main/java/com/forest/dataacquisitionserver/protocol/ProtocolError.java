package com.forest.dataacquisitionserver.protocol;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProtocolError {
    @SerializedName("code")
    private Long code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private JsonObject data;
}
