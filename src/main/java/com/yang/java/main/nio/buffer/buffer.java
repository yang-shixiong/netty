package com.yang.java.main.nio.buffer;


import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/9
 */
public class buffer {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1,2,3,4,5,6});
        System.out.println("ByteBuffer: " + buffer);  // ByteBuffer: java.nio.HeapByteBuffer[pos=0 lim=6 cap=6]
        // 移动position以及limit
        buffer.position(2);
        buffer.limit(4);
        System.out.println("ByteBuffer: " + buffer);  // ByteBuffer: java.nio.HeapByteBuffer[pos=2 lim=4 cap=6]
        System.out.println("isReadOnly: " + buffer.isReadOnly()); // isReadOnly: false
        System.out.println("isDirect: " + buffer.isDirect()); // isDirect: false
        buffer.get();
        System.out.println("ByteBuffer: " + buffer);  // ByteBuffer: java.nio.HeapByteBuffer[pos=3 lim=4 cap=6]
        System.out.println("slice: " + buffer.slice()); // slice: java.nio.HeapByteBuffer[pos=0 lim=1 cap=1]
        ByteBuffer duplicate = buffer.duplicate(); // 源码 new DirectByteBuffer(this, this.markValue(), this.position(), this.limit(), this.capacity(), 0);
        System.out.println("duplicate compareTo: " + duplicate.compareTo(buffer)); // duplicate: true
        System.out.println("duplicate: " + duplicate.equals(buffer)); // duplicate: true
        System.out.println("duplicate: " + duplicate + "  ByteBuffer " + buffer); // duplicate: java.nio.HeapByteBuffer[pos=3 lim=4 cap=6]  ByteBuffer java.nio.HeapByteBuffer[pos=3 lim=4 cap=6]
        System.out.println("hasArray: " + buffer.hasArray()); // hasArray: true
        System.out.println("ByteBuffer: " + buffer);  // ByteBuffer: java.nio.HeapByteBuffer[pos=3 lim=4 cap=6]
        System.out.println("array: " + Arrays.toString(buffer.array())); // array: [1, 2, 3, 4, 5, 6]
        buffer.flip();
        System.out.println("flip ByteBuffer: " + buffer); // flip ByteBuffer: java.nio.HeapByteBuffer[pos=0 lim=3 cap=6]
        System.out.println("flip array: " + Arrays.toString(buffer.array())); // flip array: [1, 2, 3, 4, 5, 6]
        buffer.position(2);
        System.out.println("ByteBuffer: " + buffer); // ByteBuffer: java.nio.HeapByteBuffer[pos=2 lim=3 cap=6]
        buffer.compact();
        System.out.println("compact ByteBuffer: " + buffer); // compact ByteBuffer: java.nio.HeapByteBuffer[pos=1 lim=6 cap=6]
        System.out.println("compact array: " + Arrays.toString(buffer.array())); // compact array: [3, 2, 3, 4, 5, 6]
        buffer.rewind();
        System.out.println("rewind ByteBuffer: " + buffer); // rewind ByteBuffer: java.nio.HeapByteBuffer[pos=0 lim=6 cap=6]
        buffer.clear();
        System.out.println("clear ByteBuffer: " + buffer); // clear ByteBuffer: java.nio.HeapByteBuffer[pos=0 lim=6 cap=6]
        System.out.println("array: " + Arrays.toString(buffer.array())); // array: [3, 2, 3, 4, 5, 6]
    }
}