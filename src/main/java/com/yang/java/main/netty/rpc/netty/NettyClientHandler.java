package com.yang.java.main.netty.rpc.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * Description:
 * 需要继承CallAble
 *
 * @author mark
 * Date 2020/9/16
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    //上下文，因此需要正在call方法中使用，因此缓存起来
    private ChannelHandlerContext context;

    //返回的结果
    private String result;

    //客户端调用方法时，传入的参数
    private String paras;

    // 必须加同步锁，通过wait等到channelRead
    @Override
    public synchronized Object call() throws Exception {
        System.out.println("call before wait");
        context.writeAndFlush(paras);
        wait();
        System.out.println("call after wait");
        return result;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
        context = ctx;
    }

    // 必须加同步锁，完成之后通过notify告知call已收到消息
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead");
        result = msg.toString();
        notify();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    public void setParas(String paras) {
        System.out.println("set paras");
        this.paras = paras;
    }
}
