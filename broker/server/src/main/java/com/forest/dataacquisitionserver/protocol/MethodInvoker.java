package com.forest.dataacquisitionserver.protocol;

import com.google.gson.JsonElement;
import io.netty.channel.ChannelHandlerContext;

public interface MethodInvoker {
    void call(ChannelHandlerContext ctx, final JsonElement id, final JsonElement params) throws Exception;
}
