package com.yunfd.netty;

import cn.hutool.core.lang.Validator;
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
 * @Version 3.5
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
        System.out.println("ip+port:" + socketAddress);
        String ipPort = socketAddress.toString().split("/")[1];

        // 分析数据报包，拿到其中携带的消息
        ByteBuf byteBuf = datagramPacket.content();
        // byteBuf.readableBytes() 返回表示 ByteBuf 当前可读取的字节数，它的值等于 writerIndex - readerIndex
        byte[] dst = new byte[byteBuf.readableBytes()];
        // 把 ByteBuf 里面的数据全部读取到 dst 字节数组中，这里 dst 的大小通常等于 readableBytes()
        byteBuf.readBytes(dst);
        String msg = new String(dst);
        System.out.println("板卡消息:" + msg);

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
                insertNewTOMap(ctx, ipPort, long_id);
                insertNewTODataBase(ipPort, long_id);
            } else System.out.println("电路板longId为" + long_id + " 格式不对，被拒绝加入map和DB");
        }
        // 心跳包 heart...#XXXX#
        if (msg.contains("Heartbeat")) {
            String long_id = msg.split("#")[1];
            //刷新板卡和服务器的连接倒计时
            redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
            //System.out.println("心跳包: 来自long_id: " + long_id + " ip: " + ip);
            String reg = "^[0-9A-Fa-f]{4}$";
            if (long_id.matches(reg) && long_id.length() == 4) {
                updateMap(ctx, ipPort, long_id);
            } else System.out.println("电路板longId：" + long_id + "，格式不对，被拒绝更新");
        }

        // 烧录过程
        // if (msg.contains("OK")) {
        //     String long_id = (msg.split("#"))[1];
        //     System.out.println("LongId：" + long_id);
        //     //刷新板卡和服务器的连接倒计时
        //     redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
        //
        //     HashMap<String, Object> info = NettySocketHolder.getInfo(long_id);
        //     // count是 发送数据的次数 Bin文件烧录时转byte
        //     int count = (int) info.get("count");
        //     // System.out.println("count: " + count + 1);
        //     String filepath = (String) info.get("filePath");
        //     System.out.println("filepath：" + filepath);
        //     SendMessageToCB.recordBinOnCB(ctx, socketAddress, filepath, count + 1);
        //     NettySocketHolder.getInfo(long_id).put("count", count + 1);
        //
        // }

        // 烧录成功  置 map 中 isRecorded 为 1
        // if (msg.contains("END")) {
        //     String long_id = (msg.split("#"))[1];
        //     //刷新板卡和服务器的连接倒计时
        //     redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
        //     HashMap<String, Object> newInfo = NettySocketHolder.getInfo(long_id);
        //     newInfo.put("isRecorded", "1");
        //     NettySocketHolder.put(long_id, newInfo);
        //     //这里没有更新MySQL
        //     log.info("已更新烧录状态！ ID: " + long_id + "; isRecorded: 1");
        // }

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
        if (msg.contains("STAT")) {
            String long_id = circuitBoardService.findByCBIP(ipPort).getLongId();
            String[] str = msg.split("#");

            if (str[1].length() == 8) {
                String lightString = str[1].substring(0, 4);
                String nixieTubeString = str[1].substring(4, 8);
                System.out.println("lightString:   " + lightString);
                System.out.println("nixieTubeString:   " + nixieTubeString);
                HashMap<String, Object> newInfo = NettySocketHolder.getInfo(long_id);
                newInfo.put("lightStatus", lightString);
                newInfo.put("nixieTubeStatus", nixieTubeString);
                NettySocketHolder.put(long_id, newInfo);
                log.info("已更新lightStatus与nixieTubeStatus！long_id: " + long_id + "; status: " + str[1]);
            }
            if (Validator.isNotNull(str[2])) {
                String screenString = str[2];
                System.out.println("screenString:   " + screenString);
                HashMap<String, Object> newInfo = NettySocketHolder.getInfo(long_id);
                newInfo.put("screenStatus", screenString);
                NettySocketHolder.put(long_id, newInfo);
                log.info("已更新screenStatus！long_id: " + long_id + "; status: " + str[2]);
            }


        }

        if (msg.contains("Success")) {
            String flag = msg.split("#")[1];
            System.out.println("flag：" + flag);

            String long_id = msg.split("#")[2];
            System.out.println("long_id：" + long_id);

            //刷新板卡和服务器的连接倒计时
            redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);

            HashMap<String, Object> info = NettySocketHolder.getInfo(long_id);
            // count是 发送数据的次数 Bin文件烧录时转byte
            int count = (int) info.get("count");
            // System.out.println("count: " + count);
            String filepath = (String) info.get("filePath");
            System.out.println("filepath：" + filepath);

            // count 初始为 1
            switch (flag) {
                case "Size":
                    SendMessageToCB.recordBinOnCB(ctx, socketAddress, filepath, 2);
                    NettySocketHolder.getInfo(long_id).put("count", 3);
                    break;
                case "File":
                    SendMessageToCB.recordBinOnCB(ctx, socketAddress, filepath, count);
                    NettySocketHolder.getInfo(long_id).put("count", count + 1);
                    break;
                case "undone":
                    // String c = msg.split("#")[3];
                    // SendMessageToCB.recordBinOnCB(ctx, socketAddress, filepath, Integer.parseInt(c));
                    break;
                case "Over":
                    System.out.println("------------------板卡烧录完毕----------------");
                    redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
                    HashMap<String, Object> newInfo = NettySocketHolder.getInfo(long_id);
                    newInfo.put("isRecorded", "1");
                    NettySocketHolder.put(long_id, newInfo);
                    System.out.println("已更新烧录状态！long_id：" + long_id + "；isRecorded：1");
                    break;
            }

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("UDP通道已经连通");
    }

    // 失去连接的动作，移除mysql、redis和map中的信息
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // 移除MAP和DB里的这个连接
        String longId0 = NettySocketHolder.getLongId(ctx);
        String ipPort = NettySocketHolder.getSocketAddress(longId0).toString().split("/")[1];
        CircuitBoard circuitBoard = circuitBoardService.findByCBIP(ipPort);
        if (Validator.isNotNull(circuitBoard)) {
            String longId = circuitBoard.getLongId();
            NettySocketHolder.remove(longId);
            redisUtils.del(CommonParams.REDIS_BOARD_SERVER_PREFIX + longId);
            circuitBoardService.deleteById(circuitBoard);
            System.out.println("板卡" + longId + "失去连接，已从map和db中移除");
        }
        // 链路关闭
        ctx.channel().close();
        System.out.println("Channel is disconnected");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    /*************************************处理Map和DB***************************************/

    //初始化以及更新NettySocketHolder(Map)和数据库中的电路板记录(仅当电路板Login时)
    private HashMap<String, Object> initMap(ChannelHandlerContext ctx, String ipPort, String long_id) {
        HashMap<String, Object> info = new HashMap<>();
        // map 后面更新进redis
        info.put("ipPort", ipPort);
        info.put("ctx", ctx);
        info.put("status", "0");
        info.put("isRecorded", "0");
        info.put("buttonStatus", "");
        info.put("lightStatus", "");
        info.put("nixieTubeStatus", "");
        info.put("longId", long_id);
        info.put("screenStatus", "");
        // 之后要删除
        // info.put("count", 1);
        // info.put("filePath", "C:\\Users\\LiuZequan\\Documents\\Developer\\Java\\hdu-stm32-backend\\hdu\\upload\\udpled.bin");
        return info;
    }

    private CircuitBoard initCBDBObj(String longId, String ipPort) {
        CircuitBoard board = new CircuitBoard();
        board.setLongId(longId);
        board.setCbIpPort(ipPort);
        board.setStatus("0");
        board.setIsRecorded("0");

        return board;
    }

    private void initCBDBObjWithIdKnown(String longId, String ipPort, CircuitBoard board) {
        board.setLongId(longId);
        board.setCbIpPort(ipPort);
        board.setStatus("0");
        board.setIsRecorded("0");
    }

    private void insertNewTOMap(ChannelHandlerContext ctx, String ipPort, String long_id) {
        //新的电路板信息
        HashMap<String, Object> info = initMap(ctx, ipPort, long_id);
        // keypoint 根据 long_id 去 map 中找，如果找到了，map 更新，所有状态复原； 如果没找到，在 map 中添加。
        // 内容加入单例 Map  long_id : info
        if (NettySocketHolder.getInfo(long_id) != null)
            System.out.println("map中已有板卡 " + long_id + " ，更新map");
        else
            System.out.println("添加新板卡到 map！ longId: " + long_id + " ipPort: " + ipPort);
        NettySocketHolder.getInstance().put(long_id, info);
    }

    private void insertNewTODataBase(String ipPort, String longId) {
        // 根据 longId 去数据库中找，如果找到了，数据库更新，所有状态复原； 如果没找到，在数据库中添加。
        CircuitBoard circuitBoard = circuitBoardService.findByCBID(longId);
        // 更新数据库
        if (circuitBoard != null) {
            initCBDBObjWithIdKnown(longId, ipPort, circuitBoard);
            circuitBoardService.updateById(circuitBoard);
            log.info("数据库对longId为" + longId + "的板卡进行了更新！");
        } else {
            //向数据库中添加这个电路板
            CircuitBoard obj = initCBDBObj(longId, ipPort);
            circuitBoardService.insert(obj);
            log.info("添加新板卡到数据库！longId为: " + longId);
        }
    }

    //更新NettySocketHolder(Map)和数据库中的电路板记录(其他时候)
    private void updateMap(ChannelHandlerContext ctx, String ipPort, String long_id) {
        // 根据 long_id 去 map 中找，如果没找到，在 map 中添加。
        if (NettySocketHolder.getInfo(long_id) == null) {
            HashMap<String, Object> info = initMap(ctx, ipPort, long_id);
            //内容加入单例 Map
            updateDataBase(ipPort, long_id);
            NettySocketHolder.getInstance().put(long_id, info);
            System.out.println("添加新板卡到 map！ longId: " + long_id + " ipPort: " + ipPort);
        }
    }

    private void updateDataBase(String ipPort, String longId) {
        // 根据 longId 去数据库中找，如果找到了，数据库更新，所有状态复原； 如果没找到，在数据库中添加。
        // 更新数据库
        CircuitBoard circuitBoard = circuitBoardService.findByCBID(longId);
        if (circuitBoard == null) {
            //向数据库中添加这个电路板
            insertNewTODataBase(ipPort, longId);
        }
    }
}
