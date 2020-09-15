package com.yang.java.main.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/11
 */
public class NettyServer {
    public static void main(String[] args) {
        // 创建两个线程组，默认的子线程NioEventLoop个数为cpu核数*2，也可以自己自定数量
        // DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
        /* 最终会调用这个方法
        protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
            super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
        }         */
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        // 创建服务器端的启动对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)  // 设置两个线程组
                .channel(NioServerSocketChannel.class) // 设置服务器通道实现的类
                .option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列得到线程连接的个数
                .handler(new LoggingHandler(LogLevel.DEBUG)) // handler是设置bossGroup的，这是一个日志级别handler
                .childHandler(new ChannelInitializer<SocketChannel>() {  // childHandler是设置workerGroup的
                    // 微通道为通道设置处理器
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //addLast也就是在通道中标所有的handler字后面加（除了tail handler，这个是netty跪地必须要通道尾部）
                        ch.pipeline().addLast(new NettyServerHandler());  // 这里面添加的类必须实现ChannelHandler
                    }
                });
        System.out.println("the server is ready ");
        try {
            // 绑定端口生成future对象
            ChannelFuture channelFuture = serverBootstrap.bind(9998).sync();
            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 最终关闭两个线程组
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }
}

class NettyServerHandler extends ChannelInboundHandlerAdapter {

    // 有连接上来
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive current thread is: " + Thread.currentThread().getName());
    }

    /**
     * 读取数据
     *
     * @param ctx 上下文含有pipeline，channel等信息
     * @param msg 客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("channelRead current thread is: " + Thread.currentThread().getName());
        ByteBuf buffer = (ByteBuf) msg;
        System.out.println("receive message is: " + buffer.toString(CharsetUtil.UTF_8));
        System.out.println("client address is: " + ctx.channel().remoteAddress());
    }

    // 数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete current thread is: " + Thread.currentThread().getName());
        ctx.writeAndFlush(Unpooled.copiedBuffer("server received the message", CharsetUtil.UTF_8));
    }

    // 异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 关闭
        cause.printStackTrace();
        ctx.close();
    }
}

class NewNettyServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 1 用户自定义的普通任务
        System.out.println("execute start time: " + LocalDateTime.now());
        ctx.channel().eventLoop().execute(() -> lateSend(ctx, "execute"));

        // 2 用户自定义的定时场景
        System.out.println("schedule start time: " + LocalDateTime.now());
        ctx.channel().eventLoop().schedule(() -> lateSend(ctx, "schedule"), 5, TimeUnit.SECONDS);
    }

    private void lateSend(ChannelHandlerContext ctx, String message) {
        try {
            System.out.println(message + " lateSend time: " + LocalDateTime.now());
            System.out.println(message + " current thread: " + Thread.currentThread().getName());
            Thread.sleep(5 * 1000);
            ctx.writeAndFlush(Unpooled.copiedBuffer("wait 5s", CharsetUtil.UTF_8));
            System.out.println(message + " current channel" + ctx.channel().hashCode());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}