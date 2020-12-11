package com.forest.dataacquisitionserver.protocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestPacket {
    @SerializedName("version")
    private ProtocolVersion version;
    @SerializedName("method")
    private PacketType type;
    @SerializedName("id")
    private JsonElement id;
    @SerializedName("params")
    private JsonElement params;
}
