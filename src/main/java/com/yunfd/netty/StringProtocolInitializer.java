package com.yunfd.netty;/**
 * Created with IntelliJ IDEA.
 * User: xuanjiazhen
 * Date: 2018/3/12
 * Time: 上午10:49
 */

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


/**
 * @author xuan
 * @version 1.0.0
 */
// channel 和客户端进行数据读写的通道 Initializer 初始化 负责添加别的handler
@Component
@Qualifier("springProtocolInitializer")
public class StringProtocolInitializer extends ChannelInitializer<NioDatagramChannel> {

    @Autowired
    StringDecoder stringDecoder;

    @Autowired
    StringEncoder stringEncoder;

    @Autowired
    UDPServerHandler UDPServerHandler;

//    private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();


    @Override
    protected void initChannel(NioDatagramChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", stringDecoder);
        pipeline.addLast("handler", UDPServerHandler);
        pipeline.addLast("encoder", stringEncoder);

        // ch.pipeline().addLast(new UDPServerHandler());

        /*
        *    定义的服务器端读事件的时间，
        *       当客户端 75s 时间没有往服务器写数据（服务器端就是读操作）则触发IdleStateEvent事件。
        *
        *    服务器端写事件的时间， 0 为未设置
        *       当服务器端 7s 的时间没有向客户端写数据，则触发IdleStateEvent事件。
        *       当客户端没有往服务器端写数据和服务器端没有往客户端写数据 10s 的时间，则触发IdleStateEvent事件。
        * */
//        pipeline.addLast("timeout", new IdleStateHandler(75,0,0, TimeUnit.SECONDS));
//        pipeline.addLast(new IdleStateHandler(75,0,0, TimeUnit.SECONDS));

    }

    public StringDecoder getStringDecoder() {
        return stringDecoder;
    }

    public void setStringDecoder(StringDecoder stringDecoder) {
        this.stringDecoder = stringDecoder;
    }

    public StringEncoder getStringEncoder() {
        return stringEncoder;
    }

    public void setStringEncoder(StringEncoder stringEncoder) {
        this.stringEncoder = stringEncoder;
    }

    public UDPServerHandler getServerHandler() {
        return UDPServerHandler;
    }

    public void setServerHandler(UDPServerHandler UDPServerHandler) {
        this.UDPServerHandler = UDPServerHandler;
    }

}

