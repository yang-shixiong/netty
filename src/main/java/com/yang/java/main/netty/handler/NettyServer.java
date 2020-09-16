package com.yang.java.main.netty.handler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;

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
                        ch.pipeline().addLast(new IntegerDecoder());  //
                        ch.pipeline().addLast(new NettyServerHandler2());  // 这里面添加的类必须实现ChannelHandler
                        ch.pipeline().addLast(new NettyServerHandler());  // 这里面添加的类必须实现ChannelHandler
                    }
                });
        System.out.println("the server is ready ");
        try {
            // 绑定端口生成future对象
            ChannelFuture channelFuture = serverBootstrap.bind(9996).sync();
            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 最终关闭两个线程组
//            bossGroup.shutdownGracefully();
//            workGroup.shutdownGracefully();
        }

    }
}

class NettyServerHandler implements ChannelInboundHandler {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelUnregistered");
    }

    // 有连接上来
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive current thread is: " + Thread.currentThread().getName());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive");
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
        System.out.println("channelRead receive message is: " + msg);
    }

    // 数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete current thread is: " + Thread.currentThread().getName());
        ctx.write(Unpooled.copiedBuffer("server feedback", CharsetUtil.UTF_8));
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("userEventTriggered, evt: " + evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelWritabilityChanged");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved");
    }

    // 异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        // 关闭
        ctx.close();
    }
}

class NettyServerHandler2 extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerHandler2 channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerHandler2 channelUnregistered");
    }

    // 有连接上来
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerHandler2 channelActive current thread is: " + Thread.currentThread().getName());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerHandler2 channelInactive");
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
        System.out.println("NettyServerHandler2 channelRead receive message is: " + msg);
        ctx.fireChannelRead(msg);
    }

    // 数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerHandler2 channelReadComplete current thread is: " + Thread.currentThread().getName());
        ctx.write(Unpooled.copiedBuffer("server feedback", CharsetUtil.UTF_8));
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("NettyServerHandler2 userEventTriggered, evt: " + evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerHandler2 channelWritabilityChanged");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerHandler2handlerAdded");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("NettyServerHandler2 handlerRemoved");
    }

    // 异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("NettyServerHandler2 exceptionCaught");
        // 关闭
        ctx.close();
    }
}

class IntegerDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() >= 4){
            out.add(in.readInt());
        }
    }
}

class NettyOutboundHandler implements ChannelOutboundHandler{
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("ChannelOutboundHandler server bind, localAddress: " + localAddress);
        System.out.println(promise);
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("ChannelOutboundHandler client connect, localAddress: " + localAddress + " client remoteAddress: " + remoteAddress);
        System.out.println(promise);
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("ChannelOutboundHandler client disconnect, remoteAddress: " + ctx.channel().remoteAddress());
        System.out.println(promise);
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("ChannelOutboundHandler client close, remoteAddress: " + ctx.channel().remoteAddress());
        System.out.println(promise);
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("ChannelOutboundHandler client close, deregister, remoteAddress: " + ctx.channel().remoteAddress());
        System.out.println(promise);
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ChannelOutboundHandler client read, remoteAddress: " + ctx.channel().remoteAddress());
        ctx.read();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        byte[] array = ((ByteBuf) msg).array();
        System.out.println("ChannelOutboundHandler write: " + ctx.channel().remoteAddress() + " msg: " + new String(array));
        System.out.println(promise);
        ctx.write(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ChannelOutboundHandler client flush, remoteAddress " + ctx.channel().remoteAddress());
        ctx.flush();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ChannelOutboundHandler client handlerAdded, remoteAddress " + ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ChannelOutboundHandler client handlerRemoved, remoteAddress " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("ChannelOutboundHandler client exceptionCaught, remoteAddress " + ctx.channel().remoteAddress() + " cause: " + cause.getMessage());
        ctx.close();
    }

}