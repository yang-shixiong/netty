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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/10
 */
public class MappedByteBufferDemo {
    public static void main(String[] args) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("d:\\file01.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();
        /**
         * 参数1：制定该channel的模式：
         *      public static final MapMode READ_ONLY = new MapMode("READ_ONLY");   只读模式
         *      public static final MapMode READ_WRITE =new MapMode("READ_WRITE");  读写模式
         *      public static final MapMode PRIVATE =new MapMode("PRIVATE");        写时复制
         * 参数2：可以直接修改的起始位置
         * 参数3：是映射到内存的大小，即当前文件有多少个字节可以映射到内存中，，一共有size大小的字节映射到内存
         */
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 12);
        map.put(0, (byte) 'A');
        map.put(1, (byte) 'B');
        randomAccessFile.close();  // 修改值之后文件内容（有两个空格）：‘ABll, mark  ’
    }
}