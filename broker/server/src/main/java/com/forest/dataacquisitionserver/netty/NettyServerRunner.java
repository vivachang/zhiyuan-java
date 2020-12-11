package com.forest.dataacquisitionserver.netty;

import com.forest.dataacquisitionserver.protocol.invoker.*;
import com.forest.dataacquisitionserver.protocol.MessageHandlerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * netty启动类
 */
@Order(0)
@Component
@RequiredArgsConstructor
public class NettyServerRunner implements CommandLineRunner {
    private final NettyService nettyService;
    private final MessageHandlerMapper messageHandlerMapper;
    private final AuthInvoker authInvoker;
    private final PingInvoker pingInvoker;
    private final ReportInvoker reportInvoker;
    private final RepairReportInvoker repairReportInvoker;
    private final TimeInvoker timeInvoker;
    private final BatteryInvoker batteryInvoker;

    @Override
    public void run(String... args) throws Exception {
        // 在map中加入消息处理类
        messageHandlerMapper.putInvoker("auth", authInvoker)
                .putInvoker("ping", pingInvoker)
                .putInvoker("report", reportInvoker)
                .putInvoker("repairReport", repairReportInvoker)
                .putInvoker("battery", batteryInvoker)
                .putInvoker("time", timeInvoker);
        // 启动netty
        nettyService.start();
    }
}