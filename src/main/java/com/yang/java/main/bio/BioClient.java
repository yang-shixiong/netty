package com.yang.java.main.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/9
 */
public class BioClient {

    public static void main(String[] args) throws IOException {
        // 绑定服务器
        Socket client = new Socket("127.0.0.1", 9999);
        // 创建输入
        Scanner scanner = new Scanner(System.in);
        System.out.println("please input the content: ");
        for (; ; ) {
            if (scanner.hasNext()) {
                String str = scanner.next();
                System.out.println(str);
                OutputStream outputStream = client.getOutputStream();
                outputStream.write(str.getBytes());
            }
        }
    }
}