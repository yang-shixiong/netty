package com.yang.java.main.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/9
 */
public class BioServer {

    public static void main(String[] args) throws IOException {
        // 创建一个线程池，当有socket链接，就创建一个线程，机型通信
        ExecutorService executorService = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("socket server started.");
        // 循环监听
        do {
            System.out.println("start to listen, current thread is :" + Thread.currentThread().getName());
            // 接受连接
            Socket socket = serverSocket.accept();
            // 创建一个新的线程去处理接受的连接
            executorService.execute(() -> handler(socket));
        } while (true);
    }

    private static void handler(Socket socket) {
        System.out.println("current thread is :" + Thread.currentThread().getName());
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] bytes = new byte[1024];
            int read;
            do {
                read = inputStream.read(bytes);
                System.out.println("receive the message is: " + new String(bytes));
            } while (read != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}