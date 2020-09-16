package com.yang.java.main.netty.eventLoop;

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
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

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
                        ch.pipeline().addLast(new NewNettyServerHandler());  // 这里面添加的类必须实现ChannelHandler
                    }
                });
        System.out.println("the server is ready ");
        try {
            // 绑定端口生成future对象
            ChannelFuture channelFuture = serverBootstrap.bind(8888).sync();
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

class NewNettyServerHandler extends ChannelInboundHandlerAdapter {

    static final EventExecutorGroup group = new DefaultEventExecutorGroup(16);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        group.submit(() -> lateSend(ctx));
        ctx.channel().eventLoop().execute(() -> lateSend(ctx));
        ctx.writeAndFlush(Unpooled.copiedBuffer("immediately", CharsetUtil.UTF_8));
    }

    private void lateSend(ChannelHandlerContext ctx) {
        try {
            Thread.sleep(10 * 1000);
            ctx.writeAndFlush(Unpooled.copiedBuffer("wait 10s", CharsetUtil.UTF_8));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}