package com.crypto.jtrade.front.provider.websocket.handler;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Stream information header processing
 *
 * @author 0xWill
 **/
@Slf4j
@Component
public class HeadHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static ConcurrentHashMap<String, String> SESSION_HOST_MAP = new ConcurrentHashMap<>(1024);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String sessionId = ctx.channel().id().asLongText();
        String host = request.headers().get("host");
        SESSION_HOST_MAP.put(sessionId, host);

        ctx.fireChannelRead(request.retain());
    }

}
