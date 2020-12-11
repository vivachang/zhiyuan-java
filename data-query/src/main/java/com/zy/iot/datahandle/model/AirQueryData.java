package com.zy.iot.datahandle.model;


import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 数据查询返回对象
 */
public class AirQueryData {
    private String deviceId;
    private String monitorId;
    private String projectId;
    private String timestamp;
    private String humidity;
    private String temperature;
    private String formaldehyde;
    private String PM25;
    private String CO2;
    private String TVOC;
    private JSONArray red;
    private String status;
    private String stageId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getFormaldehyde() {
        return formaldehyde;
    }

    public void setFormaldehyde(String formaldehyde) {
        this.formaldehyde = formaldehyde;
    }

    @JsonProperty("PM25")
    public String getPM25() {
        return PM25;
    }

    public void setPM25(String PM25) {
        this.PM25 = PM25;
    }

    @JsonProperty("CO2")
    public String getCO2() {
        return CO2;
    }

    public void setCO2(String CO2) {
        this.CO2 = CO2;
    }

    @JsonProperty("TVOC")
    public String getTVOC() {
        return TVOC;
    }

    public void setTVOC(String TVOC) {
        this.TVOC = TVOC;
    }

    public JSONArray getRed() {
        return red;
    }

    public void setRed(JSONArray red) {
        this.red = red;
    }
}
