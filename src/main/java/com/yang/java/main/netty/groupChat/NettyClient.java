package com.yang.java.main.netty.groupChat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/12
 */
public class NettyClient {

    public static void main(String[] args) {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder1", new StringEncoder());
                        pipeline.addLast(new NettyClientHandler());
                    }
                });
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(new InetSocketAddress("localhost", 9999)).sync();
            Channel channel = channelFuture.channel();
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()){
                String str = scanner.nextLine();
                channel.writeAndFlush(str + "\r\n");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}

class NettyClientHandler extends SimpleChannelInboundHandler<String>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("receive message: " + msg);
    }
}
