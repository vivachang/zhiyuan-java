package com.forest.dataacquisitionserver.protocol;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ProtocolProperty.class)
public class ProtocolConfiguration {
}
