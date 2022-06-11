package com.yunfd.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * @Description
 * @Author LiuZequan
 * @Date 2022/4/13 11:27
 * @Version 1.0
 */

@Component
public class UDPServer {
    @Autowired
    @Qualifier("Bootstrap")
    private Bootstrap b;

    @Autowired
    @Qualifier("udpSocketAddress")
    private InetSocketAddress address;

    private ChannelFuture serverChannelFuture;

    @PostConstruct
    public void start() {
        try {
            System.out.println("Starting udpServer at " + address);
            serverChannelFuture = b.bind(address.getAddress(),address.getPort()).sync();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Starting udpServer exception");
        }
    }

    @PreDestroy
    private void stop() throws Exception{
        serverChannelFuture.channel().closeFuture().sync();
    }

    public Bootstrap getB() {
        return b;
    }

    public void setB(Bootstrap b) {
        this.b = b;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }
}
