package com.yunfd.netty;

import com.yunfd.service.CircuitBoardService;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * @Description
 * @Author LiuZequan
 * @Date 2022/4/13 11:32
 * @Version 2.0
 */


/*
    单例模式实现此类
    {
	“1232434”（ip）:{“ctx”:ctx , ”status”:0 , ”isRecorded”:0 , ”buttonStatus”:101…..1 , ”lightStatus”: 0000...,  id："longid"},
	“1232435”:{“ctx”:ctx , ”status”:1 },
    }


    单例模式实现此类
    {
	“1232434”
	（longid）:{“ctx”:ctx , ”status”:0 , ”isRecorded”:0 , ”buttonStatus”:101…..1 , ”lightStatus”: 0000...,  ip："ip"},

	“1232435”:{“ctx”:ctx , ”status”:1 },
    }
 */

//线程安全的懒汉模式
public class NettySocketHolder {

    private CircuitBoardService circuitBoardService;

    // 单例实例 单例实例中的一个属性是 map 故保证了该属性的单例性
    private static NettySocketHolder Holder = null;

    // 单例实例的属性 keypoint 目前为止100块板子
    private static final HashMap<String, HashMap<String, Object>> nettySocketHolder = new HashMap<>(100);

    private NettySocketHolder() {
    }

    // DCL 单例模式
    public static NettySocketHolder getInstance() {
        if (null == Holder) {
            synchronized (NettySocketHolder.class) {
                if (null == Holder) {
                    Holder = new NettySocketHolder();
                }
            }
        }
        return Holder;
    }


    //============================直接对属性进行操作==========================

    /**
     * 将信息存入map
     */
    public static void put(String longId, HashMap<String, Object> info) {
        getInstance().nettySocketHolder.put(longId, info);
    }

    /**
     * 从map中替换
     */
    public static void replace(String longId, HashMap<String, Object> info) {
        getInstance().nettySocketHolder.replace(longId, info);
    }

    /**
     * 从map中删除
     */
    public static void remove(String longId) {
        getInstance().nettySocketHolder.remove(longId);
    }

    /**
     * 返回 info
     */
    public static HashMap<String, Object> getInfo(String longId) {
        return getInstance().nettySocketHolder.get(longId);
    }


    /**
     * 返回 ctx
     */
    public static ChannelHandlerContext getCtx(String longId) {
        return (ChannelHandlerContext) NettySocketHolder.getInfo(longId).get("ctx");
    }

    /**
     * 返回 InetSocketAddress
     */
    public static InetSocketAddress getSocketAddress(String longId) {
        return new InetSocketAddress((String) NettySocketHolder.getInfo(longId).get("ip"), (int) NettySocketHolder.getInfo(longId).get("port"));
    }
}