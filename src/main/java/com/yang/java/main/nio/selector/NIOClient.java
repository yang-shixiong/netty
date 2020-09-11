package com.yang.java.main.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/10
 */
public class NIOClient {

    public static void main(String[] args) throws IOException {
        // 获取一个socketChannel
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        // 连接服务器
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
        while (!socketChannel.finishConnect()){
            System.out.println("not finish connect, do other");
        }
        String str = "hello, service";
        ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes());
        socketChannel.write(byteBuffer);
        socketChannel.close();
    }
}
