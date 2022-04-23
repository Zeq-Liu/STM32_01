package com.yunfd.config;

import com.yunfd.netty.StringProtocolInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Description
 * @Author LiuZequan
 * @Date 2022/4/13 11:30
 * @Version 1.0
 */


@Configuration
public class NettyConfig {

    //读取yml中配置
    // @Value("${boss.thread.count}")
    // private int bossCount;
    //
    // @Value("${worker.thread.count}")
    // private int workerCount;
    //
    // @Value("${tcp.port}")
    // private int tcpPort;

    @Value("${udp.port}")
    private int udpPort;

    @Value("${udp.ip}")
    private String udpIp;

    @Value("${so.keepalive}")
    private boolean keepAlive;

    @Value("${so.backlog}")
    private int backlog;

    @Autowired
    @Qualifier("springProtocolInitializer")
    private StringProtocolInitializer protocolInitializer;

    // bootstrap配置
    // 服务器端启动器，复制组装netty组件，启动服务器
/*   @SuppressWarnings("unchecked")
  @Bean(name = "serverBootstrap")
  public ServerBootstrap bootstrap() {
    ServerBootstrap b = new ServerBootstrap();
    // BossEventLoop, WorkerEventLoop(selector, thread)
    b.group(bossGroup(), workerGroup())
            // 选择服务器的ServerSocketChannel实现
            .channel(NioServerSocketChannel.class)
            // boss 负责处理连接 work(child) 负责处理读写，决定了worker(child) 能执行哪些操作(handler)
            .childHandler(protocolInitializer);
    Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
    Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
    for (@SuppressWarnings("rawtypes") ChannelOption option : keySet) {
      b.option(option, tcpChannelOptions.get(option));
    }
    return b;
  } */
    @SuppressWarnings("unchecked")
    @Bean(name = "Bootstrap")
    public Bootstrap bootstrap() {
        Bootstrap b = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        b.group(group)
                .channel(NioDatagramChannel.class)
                .handler(protocolInitializer)
                .option(ChannelOption.SO_KEEPALIVE, keepAlive)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, backlog);

        return b;

    }

    // @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    // public NioEventLoopGroup bossGroup() {
    //   return new NioEventLoopGroup(bossCount);
    // }
    //
    // @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    // public NioEventLoopGroup workerGroup() {
    //   return new NioEventLoopGroup(workerCount);
    // }
    //
    // @Bean(name = "tcpSocketAddress")
    // public InetSocketAddress tcpPort() {
    //   return new InetSocketAddress(tcpPort);
    // }

    @Bean(name = "udpSocketAddress")
    public InetSocketAddress address() {
        return new InetSocketAddress(udpIp, udpPort);
    }

    // @Bean(name = "tcpChannelOptions")
    // public Map<ChannelOption<?>, Object> tcpChannelOptions() {
    //   Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
    //   // options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
    //   options.put(ChannelOption.SO_REUSEADDR, true);
    //   options.put(ChannelOption.SO_BACKLOG, backlog);
    //   return options;
    // }

    @Bean(name = "stringEncoder")
    public StringEncoder stringEncoder() {
        return new StringEncoder();
    }

    @Bean(name = "stringDecoder")
    public StringDecoder stringDecoder() {
        return new StringDecoder();
    }

    /**
     * Necessary to make the Value annotations work.
     *
     * @return
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
