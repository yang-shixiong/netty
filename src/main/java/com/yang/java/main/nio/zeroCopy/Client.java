package com.yang.java.main.nio.zeroCopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/11
 */
public class Client {

    public static void main(String[] args) throws IOException {
        zeroCopy();
        tradition();
    }

    public static void zeroCopy() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
        FileInputStream inputStream = new FileInputStream("D:\\package\\netty-4.1.zip");
        FileChannel channel = inputStream.getChannel();
        System.out.println("file size: " + channel.size()); // file size: 5375946
        long startTime = System.currentTimeMillis();
        long l = channel.transferTo(0, channel.size(), socketChannel);
        System.out.println("send the total size: " + l + "用时：=" + (System.currentTimeMillis() - startTime)); // send the total size: 5375946用时�?=5
        channel.close();
        inputStream.close();
        socketChannel.close();
    }

    public static void tradition() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
        FileInputStream inputStream = new FileInputStream("D:\\package\\netty-4.1.zip");
        FileChannel channel = inputStream.getChannel();
        System.out.println("file size: " + channel.size());  // file size: 5375946
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int read;
        long startTime = System.currentTimeMillis();
        do {
            read = channel.read(buffer);
            socketChannel.write(buffer);
            buffer.rewind();
        } while (read != -1);
        System.out.println("用时：=" + (System.currentTimeMillis() - startTime)); // 用时：=12
        channel.close();
        inputStream.close();
        socketChannel.close();
    }
}
