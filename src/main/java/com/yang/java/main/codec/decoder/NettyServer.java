package com.yang.java.main.codec.decoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;
import java.util.List;
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
                        ch.pipeline().addLast(new ToIntegerReplayingDecoder());
                        ch.pipeline().addLast(new IntegerToByteEncoder());
                        ch.pipeline().addLast(new NettyServerHandler());  // 这里面添加的类必须实现ChannelHandler
                    }
                });
        System.out.println("the server is ready ");
        // 绑定端口生成future对象
        ChannelFuture channelFuture = serverBootstrap.bind(9999);
        // 对关闭通道进行监听
        try {
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

    /**
     * 读取数据
     *
     * @param ctx 上下文含有pipeline，channel等信息
     * @param msg 客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("receive message is: " +  msg.toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1);
        buffer.writeInt(2);
        buffer.writeInt(3);
        ctx.writeAndFlush(buffer);
    }

    // 异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 关闭
        System.out.println("error: " + cause.getMessage());
        ctx.close();
    }
}

class ToIntegerDecoder extends ByteToMessageDecoder{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() >= 4){
            out.add(in.readInt());
        }
    }
}

class ToIntegerReplayingDecoder extends ReplayingDecoder<Void>{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.add(in.readInt());
    }
}

class SafeByteToMessageDecoder extends ByteToMessageDecoder{

    private static final int MAX_FRAME_SIZE = 2;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readableBytes = in.readableBytes();
        if(readableBytes > MAX_FRAME_SIZE){
            in.skipBytes(readableBytes);
            // 可以选择抛出异常,会被下一个handler的异常捕捉接收
//            throw new TooLongFrameException("frame to big");
            out.add(10);
        }
        if(in.readableBytes() >= 4){
            out.add(in.readInt());
        }
    }
}

class IntegerToByteEncoder extends MessageToByteEncoder<Integer>{

    @Override
    protected void encode(ChannelHandlerContext ctx, Integer msg, ByteBuf out) throws Exception {
        out.writeInt(msg);
    }
}
