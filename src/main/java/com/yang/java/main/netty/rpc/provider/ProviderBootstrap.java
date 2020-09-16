package com.yang.java.main.netty.rpc.provider;

import com.yang.java.main.netty.rpc.netty.NettyServer;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/16
 */
public class ProviderBootstrap {

    public static void main(String[] args) {
        NettyServer.startServer("127.0.0.1", 7000);
    }
}
