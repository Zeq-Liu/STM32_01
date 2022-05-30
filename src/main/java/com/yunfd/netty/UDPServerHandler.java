package com.yunfd.netty;

import cn.hutool.core.util.StrUtil;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.CircuitBoard;
import com.yunfd.service.CircuitBoardService;
import com.yunfd.util.RedisUtils;
import com.yunfd.util.core.SendMessageToCB;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @Description
 * @Author LiuZequan
 * @Date 2022/4/13 11:32
 * @Version 2.0
 */


@Component
@Qualifier("UDPServerHandler")
@Slf4j
@ChannelHandler.Sharable
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Autowired
    protected CircuitBoardService circuitBoardService;
    @Autowired
    private RedisUtils redisUtils;

    // 手动注入Service
    // 通过@PostConstruct实现初始化bean之前进行的操作
    @PostConstruct
    public void init() {
        UDPServerHandler UDPServerHandler = this;
        UDPServerHandler.circuitBoardService = this.circuitBoardService;
    }

    @Override
    //keypoint 接受电路板传来的信息
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        System.out.println("channelRead0已运行，数据报包为：" + datagramPacket);
        // SendMessageToCB.datagramPacketMsg = datagramPacket;

        // 分析数据报包，获取板卡IP地址与端口
        InetSocketAddress socketAddress = datagramPacket.sender();
        String ip = socketAddress.getAddress().getHostAddress();
        // int port = socketAddress.getPort();
        // System.out.println("板卡的IP地址为：" + ip + "，PORT端口：" + port);
        //System.out.println("板卡网络地址：" + socketAddress);

        // 分析数据报包，拿到其中携带的消息
        ByteBuf byteBuf = datagramPacket.content();
        // byteBuf.readableBytes() 返回表示 ByteBuf 当前可读取的字节数，它的值等于 writerIndex - readerIndex
        byte[] dst = new byte[byteBuf.readableBytes()];
        // 把 ByteBuf 里面的数据全部读取到 dst 字节数组中，这里 dst 的大小通常等于 readableBytes()
        byteBuf.readBytes(dst);
        String msg = new String(dst);
        System.out.println("板卡消息:" + msg);

        // if (new String(msg).contains("111")) {
        //     ByteBuf bf = Unpooled.copiedBuffer("NNN", CharsetUtil.UTF_8);
        //     ctx.writeAndFlush(new DatagramPacket(bf, socketAddress));
        //     System.out.println("发送成功");
        // }


        // 电路板客户端登录
        if (msg.contains("Login")) {
            String[] logins = msg.split("Login");
            System.out.println("logins: " + Arrays.toString(logins));

            // todo：long_id 是指什么id，如何生成？
            String long_id = (logins[1].split("#"))[1];

            System.out.println("LongId：" + long_id);

            String reg = "^[0-9A-Fa-f]{4}$";
            // 刷新板卡和服务器的连接倒计时
            redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
            // 往NettySocketHolder(Map)和数据库中初始化这块电路板
            if (long_id.matches(reg) && long_id.length() == 4) {
                insertNewTOMap(ctx, socketAddress, long_id);
                insertNewTODataBase(ctx, socketAddress, long_id);
            } else System.out.println("电路板longId为" + long_id + " 格式不对，被拒绝加入map和DB");
        }
        // 心跳包
        if (msg.contains("Heartbeat")) {// heart...#XXXX#
            String[] heartbeats = msg.split("Heartbeat");
            String long_id = heartbeats[1].split("#")[1];
            //刷新板卡和服务器的连接倒计时
            redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
            //System.out.println("心跳包: 来自long_id: " + long_id + " ip: " + ip);
            String reg = "^[0-9A-Fa-f]{4}$";
            if (long_id.matches(reg) && long_id.length() == 4) {
                updateMap(ctx, socketAddress, long_id);
                // SendMessageToCB.recordBitOnCB(ctx, socketAddress, "C:\\Users\\LiuZequan\\Documents\\Developer\\Java\\hdu-fpga-backend\\hdu\\upload\\led.bin", 0);
            } //else System.out.println("电路板longId为" + long_id + " 格式不对，被拒绝更新");
        }

        // 烧录过程
        if (msg.contains("OK")) {
            String long_id = (msg.split("#"))[1];
            System.out.println("LongId：" + long_id);
            //刷新板卡和服务器的连接倒计时
            redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);

            HashMap<String, Object> info = NettySocketHolder.getInfo(long_id);
            // count是 发送数据的次数 bit文件烧录时转byte
            int count = (int) info.get("count");
            // System.out.println("count: " + count + 1);
            String filepath = (String) info.get("filePath");
            System.out.println("filepath：" + filepath);
            SendMessageToCB.recordBitOnCB(ctx, socketAddress, filepath, count + 1);
            NettySocketHolder.getInfo(long_id).put("count", count + 1);

        }

        // 烧录成功  置 map 中 isRecorded 为 1
        if (msg.contains("END")) {
            String long_id = (msg.split("#"))[1];
            //刷新板卡和服务器的连接倒计时
            redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
            HashMap<String, Object> newInfo = NettySocketHolder.getInfo(long_id);
            newInfo.put("isRecorded", "1");
            NettySocketHolder.put(long_id, newInfo);
            //这里没有更新MySQL
            log.info("已更新烧录状态！ ID: " + long_id + "; isRecorded: 1");
        }

        // // 收到关闭链路消息
        // if (msg.contains("Bye")) {
        //     // channelInactive(ctx);
        //     log.info("bye!");
        // }
        //
        // if (msg.contains("NICE")) {
        //
        // }

        // 运行成功  置 map 中 lightStatus 为 lightStatus
        if (msg.contains("STAT")) { // SIG
            String long_id = circuitBoardService.findByCBIP(ip).getLongId();
            String[] stats = msg.split("STAT");
            String[] str = stats[1].split("#");

            // keypoint 旧板子
            if (str[1].length() == 8) {
                String lightString = str[1];
                log.info("lightString:   " + lightString);
                HashMap<String, Object> newInfo = NettySocketHolder.getInfo(long_id);
                newInfo.put("lightStatus", lightString);
                newInfo.put("nixieTubeStatus", "");
                NettySocketHolder.put(long_id, newInfo);
            }
            // keypoint 新板子 2021
            else if (str[1].length() == 18) {
                String lightString = str[1].substring(0, 8);
                String nixieTubeString = str[1].substring(8, 16);
                // 接收到的数码管信息为是反的，需要翻转
                String nixieTubeStringReversed = StrUtil.reverse(nixieTubeString);

                log.info(long_id + "  lightString: " + lightString + "  ,nixieTubeString: " + nixieTubeStringReversed);
                HashMap<String, Object> newInfo = NettySocketHolder.getInfo(long_id);
                newInfo.put("lightStatus", lightString);
                newInfo.put("nixieTubeStatus", nixieTubeStringReversed);
                NettySocketHolder.put(long_id, newInfo);
            }
            log.info("已更新light状态！ID: " + long_id + "; status: " + str[1]);
        }

        if (msg.contains("Success")) {
            // SendMessageToCB.backtrack = msg.split("Success")[1];
            String flag = msg.split("#")[1];
            System.out.println("flag：" + flag);

            String long_id = msg.split("#")[2];
            System.out.println("long_id：" + long_id);

            //刷新板卡和服务器的连接倒计时
            redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);

            HashMap<String, Object> info = NettySocketHolder.getInfo(long_id);
            // count是 发送数据的次数 bit文件烧录时转byte
            int count = (int) info.get("count");
            // System.out.println("count: " + count);
            String filepath = (String) info.get("filePath");
            System.out.println("filepath：" + filepath);

            // count 初始为 1
            switch (flag) {
                case "Size":
                    SendMessageToCB.recordBitOnCB(ctx, socketAddress, filepath, 2);
                    NettySocketHolder.getInfo(long_id).put("count", 3);
                    break;
                // case "MD5":
                //     SendMessageToCB.recordBitOnCB(ctx, socketAddress, filepath, 2);
                //     NettySocketHolder.getInfo(long_id).put("count", 3);
                //     break;
                case "File":
                    SendMessageToCB.recordBitOnCB(ctx, socketAddress, filepath, count);
                    NettySocketHolder.getInfo(long_id).put("count", count + 1);
                    break;
                case "undone":
                    // String c = msg.split("#")[3];
                    // SendMessageToCB.recordBitOnCB(ctx, socketAddress, filepath, Integer.parseInt(c));
                    break;
                case "Over":
                    System.out.println("------------------板卡烧录完毕----------------");
                    break;
            }

        }


        // if (msg.contains("Over")){
        //     String long_id = msg.split("#")[1];
        //     System.out.println("long_id：" + long_id);
        //     System.out.println("--------------------------------------------------");
        //
        // }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("UDP通道已经连通");
    }

    // 失去连接的动作
    // @Override
    // public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    //     super.channelInactive(ctx);
    //     // 移除MAP和DB里的这个连接
    //     String IP = (ctx.channel().remoteAddress().toString().split("/"))[1];
    //     CircuitBoard circuitBoard = circuitBoardService.findByCBIP(IP);
    //     if (Validator.isNotNull(circuitBoard)) {
    //         String longId = circuitBoard.getLongId();
    //         NettySocketHolder.remove(longId);
    //         redisUtils.del(CommonParams.REDIS_BOARD_SERVER_PREFIX + longId);
    //         circuitBoardService.deleteById(circuitBoard);
    //         log.error("板卡" + longId + "失去连接，已从map和db中移除");
    //     }
    //     // 链路关闭
    //     ctx.channel().close();
    //     log.error("Channel is disconnected");
    // }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    /*************************************处理Map和DB***************************************/

    //初始化以及更新NettySocketHolder(Map)和数据库中的电路板记录(仅当电路板Login时)
    private HashMap<String, Object> initMap(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String long_id) {
        HashMap<String, Object> info = new HashMap<>();

        info.put("ip", socketAddress.getAddress().getHostAddress());
        info.put("port", socketAddress.getPort());
        info.put("ctx", ctx);
        info.put("status", "0");
        info.put("isRecorded", "0");
        info.put("buttonStatus", "");
        info.put("lightStatus", "");
        info.put("nixieTubeStatus", "");
        info.put("longId", long_id);
        // 之后要删除
        info.put("count", 1);
        info.put("filePath", "C:\\Users\\LiuZequan\\Documents\\Developer\\Java\\hdu-fpga-backend\\hdu\\upload\\udpled.bin");
        return info;
    }

    private CircuitBoard initCBDBObj(String longId, InetSocketAddress socketAddress) {
        CircuitBoard board = new CircuitBoard();
        board.setLongId(longId);
        board.setCbIp(socketAddress.getAddress().getHostAddress());
        board.setCbPort(socketAddress.getPort());
        board.setStatus("0");
        board.setIsRecorded("0");
        board.setLightStatus("");
        board.setSwitchButtonStatus("");
        return board;
    }

    private void initCBDBObjWithIdKnown(String longId, InetSocketAddress socketAddress, CircuitBoard board) {
        board.setLongId(longId);
        board.setCbIp(socketAddress.getAddress().getHostAddress());
        board.setCbPort(socketAddress.getPort());
        board.setStatus("0");
        board.setIsRecorded("0");
        board.setLightStatus("");
        board.setSwitchButtonStatus("");
    }

    private void insertNewTOMap(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String long_id) {
        //新的电路板信息
        HashMap<String, Object> info = initMap(ctx, socketAddress, long_id);
        // keypoint 根据 long_id 去 map 中找，如果找到了，map 更新，所有状态复原； 如果没找到，在 map 中添加。
        // 内容加入单例 Map  long_id : info
        if (NettySocketHolder.getInfo(long_id) != null)
            log.info("map中已有 " + long_id + " ，更新map");
        else
            log.info("添加新 CB 到 map！ longId: " + long_id + " remoteAddress : " + socketAddress);
        NettySocketHolder.getInstance().put(long_id, info);
    }

    private void insertNewTODataBase(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String longId) {
        // 根据 longId 去数据库中找，如果找到了，数据库更新，所有状态复原； 如果没找到，在数据库中添加。

        // 更新数据库
        CircuitBoard circuitBoard = circuitBoardService.findByCBID(longId);
        if (circuitBoard != null) {
            initCBDBObjWithIdKnown(longId, socketAddress, circuitBoard);
            circuitBoardService.updateById(circuitBoard);
            log.info("数据库更新了" + longId + "！");
        } else {
            //向数据库中添加这个电路板
            CircuitBoard obj = initCBDBObj(longId, socketAddress);
            circuitBoardService.insert(obj);
            log.info("添加新CB 到 DB！longId: " + longId);
        }
    }

    //更新NettySocketHolder(Map)和数据库中的电路板记录(其他时候)
    private void updateMap(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String long_id) {
        // 根据 long_id 去 map 中找，如果没找到，在 map 中添加。
        if (NettySocketHolder.getInfo(long_id) == null) {
            HashMap<String, Object> info = initMap(ctx, socketAddress, long_id);
            //内容加入单例 Map
            updateDataBase(ctx, socketAddress, long_id);
            NettySocketHolder.getInstance().put(long_id, info);
            log.info("添加新 CB 到 map！ longId: " + long_id + " IP : " + socketAddress.getAddress().getHostAddress());
        }
    }

    private void updateDataBase(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String longId) {
        // 根据 longId 去数据库中找，如果找到了，数据库更新，所有状态复原； 如果没找到，在数据库中添加。
        // 更新数据库
        CircuitBoard circuitBoard = circuitBoardService.findByCBID(longId);
        if (circuitBoard == null) {
            //向数据库中添加这个电路板
            insertNewTODataBase(ctx, socketAddress, longId);
        }
    }
}
