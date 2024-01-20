package com.crypto.jtrade.front.provider.websocket.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.front.provider.websocket.service.PrivateStreamManager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * private stream handler
 *
 * @author 0xWill
 **/
@Slf4j
@Component
public class PrivateHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Autowired
    private PrivateStreamManager privateStreamManager;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String sessionId = ctx.channel().id().asLongText();
        privateStreamManager.addChannel(sessionId, ctx.channel());
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String sessionId = ctx.channel().id().asLongText();
        privateStreamManager.request(sessionId, ctx.channel(), msg.text());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String sessionId = ctx.channel().id().asLongText();
        privateStreamManager.removeChannel(sessionId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String sessionId = ctx.channel().id().asLongText();
        log.warn("websocket({}) private handler exception: {}", sessionId, cause.getMessage(), cause);
    }

}
