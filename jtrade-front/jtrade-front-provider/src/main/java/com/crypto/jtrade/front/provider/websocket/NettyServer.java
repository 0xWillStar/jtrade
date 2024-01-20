package com.crypto.jtrade.front.provider.websocket;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.websocket.handler.HeadHandler;
import com.crypto.jtrade.front.provider.websocket.handler.PrivateHandler;
import com.crypto.jtrade.front.provider.websocket.handler.PublicHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * netty WebSocket server
 *
 * @author 0xWill
 **/
@Slf4j
@Component
public class NettyServer {

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private HeadHandler headHandler;

    @Autowired
    private PublicHandler publicHandler;

    @Autowired
    private PrivateHandler privateHandler;

    @PostConstruct
    public void init() {
        // start public websocket server
        start(frontConfig.getPublicWebsocketPort(), "/ws/public", false);
        // start private websocket server
        start(frontConfig.getPrivateWebsocketPort(), "/ws/private", true);
    }

    private void start(int port, String path, boolean isPrivate) {
        try {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            ServerBootstrap sb = new ServerBootstrap();
            sb.option(ChannelOption.SO_BACKLOG, 1024);
            sb.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).localAddress(port)
                .childHandler(initializer(path, isPrivate));

            ChannelFuture cf = sb.bind().sync();
            log.info("netty websocket server started: {}, {}", port, path);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (cf.channel() != null) {
                    try {
                        cf.channel().close().sync();
                        bossGroup.shutdownGracefully().sync();
                        workerGroup.shutdownGracefully().sync();
                        log.info("netty websocket server closed: {}, {}", port, path);
                    } catch (InterruptedException e) {
                        log.error("closing netty websocket server exception: {}", e.getMessage(), e);
                    }
                }
            }));
        } catch (InterruptedException e) {
            log.error("starting netty websocket server exception: {}, {}, {}", port, path, e.getMessage(), e);
        }
    }

    private ChannelInitializer initializer(String path, boolean isPrivate) {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new ChunkedWriteHandler());
                ch.pipeline().addLast(new HttpObjectAggregator(8192));
                ch.pipeline().addLast(headHandler);
                ch.pipeline().addLast(new WebSocketServerProtocolHandler(path, "WebSocket", true, 65536 * 10));
                ch.pipeline().addLast(isPrivate ? privateHandler : publicHandler);
            }
        };
    }

}
