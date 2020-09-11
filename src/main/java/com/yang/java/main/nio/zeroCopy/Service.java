package com.yang.java.main.nio.zeroCopy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/10
 */
public class Service {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", 9999));
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            int read;
            do {
                read = socketChannel.read(buffer);
                buffer.clear();
            } while (read != -1);
            System.out.println("finished read");
            socketChannel.close();
        }
    }
}
