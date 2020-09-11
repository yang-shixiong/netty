package com.yang.java.main.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/10
 */
public class NIOServer {

    public static void main(String[] args) throws IOException {
        // 创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 绑定端口，进行服务监听
        serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", 9999));
        // 设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 得到Selector对象
        Selector selector = Selector.open();
        // 将serverSocketChannel注册到selector中，并设置关心事件为accept
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 循环等待客户端的连接
        while (true){
            if(selector.select(2000) == 0){
                System.out.println("server wait for 2 seconds, but no connect");
                continue;
            }
            // 如果返回的大于0，就说明已经获取到关注的事件，然后获取关注事件的集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            // 使用迭代器
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                //判断发生的事件
                if(selectionKey.isAcceptable()){
                    // 有客户端连接进来了，生成一个Channel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("a new client is connect");
                    // 将socketChannel设置为非阻塞的
                    socketChannel.configureBlocking(false);
                    // 注册事件,同时可以为该channel关联一个ByteBuffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if(selectionKey.isReadable()){
                    // 通过selectionKey反向获取通道
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    // 获取关联的buffer
                    ByteBuffer attachment = (ByteBuffer) selectionKey.attachment();
                    channel.read(attachment);
                    System.out.println("receive message: " + new String(attachment.array()));
                    channel.close();
                }
                iterator.remove();
            }
        }
    }
}