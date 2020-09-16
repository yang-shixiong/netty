package com.yang.java.main.netty.rpc.provider;

import com.yang.java.main.netty.rpc.publicInterface.PublicInterface;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/16
 */
public class PublicInterfaceImpl implements PublicInterface {
    @Override
    public String hello(String msg) {
        System.out.println("receive from customer: " + msg);
        return "provider receive the message: " + msg;
    }
}
