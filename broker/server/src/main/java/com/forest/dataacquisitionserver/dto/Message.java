package com.forest.dataacquisitionserver.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Message {
    @SerializedName("event")
    private String event;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("clientId")
    private String clientId;
}