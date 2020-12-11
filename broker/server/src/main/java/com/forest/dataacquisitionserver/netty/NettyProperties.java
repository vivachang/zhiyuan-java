package com.forest.dataacquisitionserver.netty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {
    @NotNull
    private String tcpHost;
    @NotNull
    @Size(min = 1025, max = 65535)
    private int tcpPort;
    @NotNull
    @Min(1)
    private int bossCount;
    @NotNull
    @Min(2)
    private int workerCount;
    @NotNull
    private boolean keepAlive;
    @NotNull
    private int backlog;
    private Duration authMaxWait = Duration.parse("PT5S");

    public void setAuthMaxWait(final String text) {
        this.authMaxWait = Duration.parse("PT"+text);
    }
}