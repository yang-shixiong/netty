/**
 * Copyright(c) JingHong Technology Co., Ltd.
 * All Rights Reserved.
 * <p>
 * This software is the confidential and proprietary information of JingHong
 * Technology Co.,Ltd. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with JingHong.
 * For more information about JingHong, welcome to https://www.imagego.com
 * <p>
 * Revision History:
 * Date         Version    Name       Description
 * 2020/9/10     1.0        mark       File Creation
 */
package com.yang.java.main.nio.channel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/10
 */
public class NIOFileChannel01 {

    public static void write() throws IOException {
        String str = "hell, mark";
        // 创建一个输出流
        FileOutputStream fileOutputStream = new FileOutputStream("d:\\file01.txt");
        // 获取通道
        FileChannel channel = fileOutputStream.getChannel();
        // 创建一个缓冲区，并将str放入缓冲区
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
        System.out.println("buffer: " + buffer); // buffer: java.nio.HeapByteBuffer[pos=0 lim=10 cap=10]
        // 如果使用allocate创建最好之后flip
        ByteBuffer allocate = ByteBuffer.allocate(512);
        allocate.put(str.getBytes());
        System.out.println("allocate: " + allocate); // allocate: java.nio.HeapByteBuffer[pos=10 lim=512 cap=512]
        allocate.flip();
        System.out.println("allocate flip: " + allocate);  // allocate flip: java.nio.HeapByteBuffer[pos=0 lim=10 cap=512]
        channel.write(buffer);
        fileOutputStream.close();
        // 进行flip的必要
        /**
         * static int write(FileDescriptor fd, ByteBuffer src, long position,
         *                      boolean directIO, int alignment, NativeDispatcher nd)
         *         throws IOException
         *     {
         *         if (src instanceof DirectBuffer) {
         *             return writeFromNativeBuffer(fd, src, position, directIO, alignment, nd);
         *         }
         *
         *         // Substitute a native buffer
         *         int pos = src.position();
         *         int lim = src.limit();
         *         assert (pos <= lim);
         *         int rem = (pos <= lim ? lim - pos : 0);  // 主要看这里
         *         ByteBuffer bb;
         *         if (directIO) {
         *             Util.checkRemainingBufferSizeAligned(rem, alignment);
         *             bb = Util.getTemporaryAlignedDirectBuffer(rem, alignment);
         *         } else {
         *             bb = Util.getTemporaryDirectBuffer(rem);
         *         }
         *         try {
         *             bb.put(src);
         *             bb.flip();  // 看这里
         *             // Do not update src until we see how many bytes were written
         *             src.position(pos);
         *
         *             int n = writeFromNativeBuffer(fd, bb, position, directIO, alignment, nd);
         *             if (n > 0) {
         *                 // now update src
         *                 src.position(pos + n);
         *             }
         *             return n;
         *         } finally {
         *             Util.offerFirstTemporaryDirectBuffer(bb);
         *         }
         *     }
         */
    }

    public static void read() throws IOException {
        File file = new File("d:\\file01.txt");
        // 创建输入流
        FileInputStream fileInputStream = new FileInputStream(file);
        // 获取Channel
        FileChannel channel = fileInputStream.getChannel();
        // 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
        // 将通道内数据读入缓冲区，因为设定的缓冲区大小等于文件大小，因此一次是可以读完的，否则需要循环读取，返回值是读取的长度，如果等于-1，则说明无信息可读取
        int read = channel.read(buffer);
        System.out.println("read " + read);  // read 10
        System.out.println("read message: " + new String(buffer.array())); // read message: hell, mark
    }

    public static void transForm() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("d:\\package\\sogou_pinyin_98a.exe");
        FileOutputStream fileOutputStream = new FileOutputStream("d:\\sogou_pinyin_98a.exe");
        // 获取各个流的channel
        FileChannel fileInputChannel = fileInputStream.getChannel();
        FileChannel fileOutputChannel = fileOutputStream.getChannel();
        System.out.println("fileInputChannel isOpen: " + fileInputChannel.isOpen()); // fileInputChannel isOpen: true
        System.out.println("fileOutputChannel isOpen: " + fileOutputChannel.isOpen()); // fileOutputChannel isOpen: true
        // 使用transform进行copy一个可读的channel
        fileOutputChannel.transferFrom(fileInputChannel, 0, fileInputChannel.size());  // copy完成的文件与源文件一直
        // 关闭通道以及流
        fileInputChannel.close();
        fileOutputChannel.close();
        fileInputStream.close();
        fileOutputStream.close();
    }

    public static void position() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("d:\\file01.txt");
        FileChannel fileInputStreamChannel = fileInputStream.getChannel();
        System.out.println("fileInputStreamChannel size: " + fileInputStreamChannel.size());  // fileInputStreamChannel size: 10
        System.out.println("fileInputStreamChannel position: " + fileInputStreamChannel.position()); // fileInputStreamChannel position: 0
        fileInputStreamChannel.position(2);
        System.out.println("fileInputStreamChannel position: " + fileInputStreamChannel.position()); // fileInputStreamChannel position: 2
        ByteBuffer allocate = ByteBuffer.allocate((int) fileInputStreamChannel.size());
        // 从文件的position开始读取，一直读到结尾
        fileInputStreamChannel.read(allocate);
        System.out.println("byteBuffer: " + allocate);  // byteBuffer: java.nio.HeapByteBuffer[pos=8 lim=10 cap=10]
        System.out.println("message: " + new String(allocate.array()));  // message: ll, mark
        fileInputStreamChannel.close();
        fileInputStream.close();
    }

    public static void transferTo() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("d:\\file01.txt"); // 源文件内容：hello, mark
        FileOutputStream fileOutputStream = new FileOutputStream("d:\\file02.txt");
        // 获取各个流的channel
        FileChannel fileInputChannel = fileInputStream.getChannel();
        FileChannel fileOutputChannel = fileOutputStream.getChannel();
        System.out.println("fileInputChannel isOpen: " + fileInputChannel.isOpen());  // fileInputChannel isOpen: true
        System.out.println("fileOutputChannel isOpen: " + fileOutputChannel.isOpen());  // fileOutputChannel isOpen: true
        // 发送数据给一个可写的channel，发送位置为2（从0开始计数），发送的字节数量为3
        fileInputChannel.transferTo(2, 3, fileOutputChannel); // 发送给的文件内容：ll,
        // 关闭通道以及流
        fileInputChannel.close();
        fileOutputChannel.close();
        fileInputStream.close();
        fileOutputStream.close();
    }

    public static void main(String[] args) throws IOException {
        write();
        read();
        transForm();
        position();
        transferTo();
    }
}