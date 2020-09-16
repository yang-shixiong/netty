package com.yang.java.main.netty.rpc.consumer;

import com.yang.java.main.netty.rpc.netty.NettyClient;
import com.yang.java.main.netty.rpc.publicInterface.PublicInterface;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/16
 */
public class ClientBootstrap {

    public static void main(String[] args){

        //创建一个消费者
        NettyClient customer = new NettyClient();

        //创建代理对象
        PublicInterface provider = (PublicInterface) customer.getBean(PublicInterface.class);

        for (;; ) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //通过代理对象调用服务提供者的方法(服务)
            String res = provider.hello("hello rpc~");
            System.out.println("result: " + res);
        }
    }
}
