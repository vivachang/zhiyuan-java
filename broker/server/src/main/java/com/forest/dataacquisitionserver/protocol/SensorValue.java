package com.forest.dataacquisitionserver.protocol;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SensorValue {
    @SerializedName("PM1.0")
    private String PM1_0;
    @SerializedName("PM2.5")
    private String PM2_5;
    @SerializedName("PM10")
    private String PM10;
    @SerializedName("VOC")
    private String VOC;
    @SerializedName("CO2")
    private String CO2;
    @SerializedName("CO")
    private String CO;
    @SerializedName("HCHO")
    private String HCHO;
    @SerializedName("TEMP")
    private String TEMP;
    @SerializedName("HUM")
    private String HUM;
}
