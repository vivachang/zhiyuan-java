package com.forest.dataacquisitionserver.protocol;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponsePacket {
    @SerializedName("version")
    private ProtocolVersion version;
    @SerializedName("id")
    private JsonElement id;
    @SerializedName("error")
    private JsonElement error;
    @SerializedName("result")
    private JsonElement result;
}
