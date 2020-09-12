package com.yang.java.main.netty.groupChat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/12
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    // 创建一个通道列表，对所有连接进行记录
    public static List<Channel> channels = new ArrayList<>();

    /**
     * 连接建立，一旦连接第一个被执行
     *
     * @param ctx
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("the client " + ctx.channel().remoteAddress() + "is added");
        if (channels.size() > 0) {
            channels.forEach(channel -> channel.writeAndFlush("[the client] " + ctx.channel().remoteAddress() + " is online"));
        }
        channels.add(ctx.channel());
    }

    /**
     * 处于活动状态
     *
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("the client " + ctx.channel().remoteAddress() + "is active");
    }

    /**
     * channel处于不活动状态
     *
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("the client " + ctx.channel().remoteAddress() + "is not active");
    }

    /**
     * 断开连接
     *
     * @param ctx
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("the client " + ctx.channel().remoteAddress() + "is removed");
        Channel channel = ctx.channel();
        if (channels.size() > 0) {
            channels.forEach(ch -> ch.writeAndFlush("[the client] " + channel.remoteAddress() + "is removed"));
        }
        channels.remove(channel);
    }

    /**
     * 接受消息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        channels.forEach(ch -> {
            if(!channel.equals(ch)){
                ch.writeAndFlush("[client] " + channel.remoteAddress() + " send a message" + msg);
            }else {
                ch.writeAndFlush("[self] send a message" + msg);
            }
        });
    }

    /**
     * 捕捉异常
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
