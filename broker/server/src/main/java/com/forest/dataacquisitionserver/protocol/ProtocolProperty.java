package com.forest.dataacquisitionserver.protocol;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "protocol")
public class ProtocolProperty {
    private long reportTimestampForwardOffset;
    private long reportTimestampBackwardOffset;
}