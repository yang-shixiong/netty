//package com.yang.java.main.netty.groupChat;
//
//import io.netty.channel.*;
//import io.netty.channel.ChannelInboundHandler;
//
//import java.lang.annotation.*;
//
///**
// * Description:
// *
// * @author mark
// * Date 2020/9/15
// */
//public interface ChannelInboundHandler extends ChannelHandler {
//
//    /**
//     * 当Channel注册完成调用
//     */
//    void channelRegistered(ChannelHandlerContext ctx) throws Exception;
//
//    /**
//     * 当Channel取消注册时调用
//     */
//    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;
//
//    /**
//     * 当Channel处于活动状态时被调用
//     */
//    void channelActive(ChannelHandlerContext ctx) throws Exception;
//
//    /**
//     * 当Channel处于非活动状态时被调用
//     */
//    void channelInactive(ChannelHandlerContext ctx) throws Exception;
//
//    /**
//     * 当Channel读取数据时被调用
//     */
//    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
//
//    /**
//     * 当Channel读取完毕数据十点用
//     */
//    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;
//
//    /**
//     * 当注册事件被触发十点用
//     */
//    void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception;
//
//    /**
//     * 当这个Channel变为写通道时发生调用
//     */
//    void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception;
//
//    /**
//     * 当出现异常时调用
//     */
//    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
//}