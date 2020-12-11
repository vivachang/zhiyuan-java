package com.zy.iot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 *
 */
@Configuration
public class Constant {
    @Value("${air.data.avg}")
    public String redis_air_data_avg;

    @Value("${air.data.record}")
    public String redis_air_data_record;

    @Value("${air.data.ravg}")
    public String redis_air_data_repair_avg;

    @Value("${air.data.rrecord}")
    public String redis_air_data_repair_record;

    @Value("${air.data.device-tags}")
    public String REDIS_AIR_DEVICES_TAGS;

    @Value("${air.data.device-timestamp}")
    public String REDIS_AIR_DEVICES_TIMESTAMP;

    @Value("${air.data.device-update}")
    public String REDIS_AIR_UPDATA_DEVICES;

    @Value("${iot.client}")
    public String AUTH_CLIENT_REDIS_PREFIX;

    @Value("${air.data.prothreshold}")
    public String redis_air_data_prothreshold;

    @Value("${air.data.prostatusstage}")
    public String redis_air_data_prostatusstage;

}
