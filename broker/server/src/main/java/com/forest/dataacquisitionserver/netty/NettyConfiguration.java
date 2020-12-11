package com.forest.dataacquisitionserver.netty;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NettyProperties.class)
public class NettyConfiguration {
}