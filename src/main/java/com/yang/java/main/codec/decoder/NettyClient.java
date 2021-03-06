package com.yang.java.main.codec.decoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/11
 */
public class NettyClient {
    public static void main(String[] args) {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        // 创建客户端启动对象
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)  // 设置客户端通道的实现类
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 加入处理器
//                        ch.pipeline().addLast(new ToIntegerDecoder());
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
        System.out.println("client ready");
        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress("127.0.0.1", 9999));
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}

class NettyClientHandler extends ChannelInboundHandlerAdapter {
    // 通道就绪会触发该方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("client channel is ready, start send message");
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1);
        buffer.writeInt(2);
        buffer.writeInt(3);
        ctx.writeAndFlush(buffer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        System.out.println("receive from server: " + Arrays.toString(bytes));
        System.out.println("server address is: " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    static class ToIntegerDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if(in.readableBytes() >= 4){
                out.add(in.readInt());
            }
        }
    }
}

