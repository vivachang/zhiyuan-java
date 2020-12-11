package com.zy.iot.datahandle.model;

import lombok.Data;

/**
 * 数据查询返回对象
 */
@Data
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
}
