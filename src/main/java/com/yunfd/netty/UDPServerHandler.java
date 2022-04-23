package com.yunfd.netty;

import com.yunfd.domain.CircuitBoard;
import com.yunfd.service.CircuitBoardService;
import com.yunfd.util.RedisUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
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

        // 分析数据报包，获取板卡IP地址与端口
        InetSocketAddress socketAddress = datagramPacket.sender();
        String ip = socketAddress.getAddress().getHostAddress();
        int port = socketAddress.getPort();
        System.out.println("板卡的IP地址为：" + ip + "，PORT端口：" + port);

        // 分析数据报包，拿到其中携带的消息
        ByteBuf byteBuf = datagramPacket.content();
        byte[] msg = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msg);
        System.out.println("板卡消息:" + new String(msg));

        if (new String(msg).contains("111")) {
            ByteBuf bf = Unpooled.copiedBuffer("NNN", CharsetUtil.UTF_8);
            ctx.writeAndFlush(new DatagramPacket(bf,
                    new InetSocketAddress(ip, port)));
            System.out.println("发送成功");
            log.info("---------------------------------");
        }


        // 电路板客户端登录
        // if (new String(msg).contains("111")) {
        //     // String[] logins = datagramPacket.split("Login");
        //     // String long_id = (logins[1].split("#"))[1];
        //     String long_id= new String(msg);
        //     String reg = "^[0-9A-Fa-f]{4}$";
        //     // 刷新板卡和服务器的连接倒计时
        //     redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
        //     // 往NettySocketHolder(Map)和数据库中初始化这块电路板
        //     if (long_id.matches(reg) && long_id.length() == 4) {
        //         insertNewTOMap(ctx, long_id);
        //         insertNewTODataBase(ctx, long_id);
        //     } else log.info("电路板longId为" + long_id + " 格式不对，被拒绝加入map和DB");
        // }
/*
    // 心跳包
    if (datagramPacket.contains("Heartbeat")) {// heart...#XXXX#
      String[] heartbeats = datagramPacket.split("Heartbeat");
      String long_id = (heartbeats[1].split("#"))[1];
      //刷新板卡和服务器的连接倒计时
      redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
      log.info("心跳包: 来自long_id: " + long_id + " ip: " + ip);
      String reg = "^[0-9A-Fa-f]{4}$";
      if (long_id.matches(reg) && long_id.length() == 4) {
        updateMap(ctx, long_id);
      } else log.info("电路板longId为" + long_id + " 格式不对，被拒绝更新");
    }

    // 烧录过程
    if (datagramPacket.contains("OK")) {
      String long_id = (datagramPacket.split("#"))[1];
      //刷新板卡和服务器的连接倒计时
      redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
      HashMap<String, Object> info = NettySocketHolder.getInfo(long_id);
      // count是 发送数据的次数 bit文件烧录时转byte
      int count = (int) info.get("count");
      log.info("count: " + count);
      String filepath = (String) info.get("filePath");
      SendMessageToCB.recordBitOnCB(ctx, filepath, count + 1);
      NettySocketHolder.getInfo(long_id).put("count", count + 1);
    }

    // 烧录成功  置 map 中 isRecorded 为 1
    if (datagramPacket.contains("END")) {
      String long_id = (datagramPacket.split("#"))[1];
      //刷新板卡和服务器的连接倒计时
      redisUtils.set(CommonParams.REDIS_BOARD_SERVER_PREFIX + long_id, true, CommonParams.REDIS_BOARD_SERVER_LIMIT);
      HashMap<String, Object> newInfo = NettySocketHolder.getInfo(long_id);
      newInfo.put("isRecorded", "1");
      NettySocketHolder.put(long_id, newInfo);
      //这里没有更新MySQL
      log.info("已更新烧录状态！ ID: " + long_id + "; isRecorded: 1");
    }

    // 收到关闭链路消息
    if (datagramPacket.contains("Bye")) {
      // channelInactive(ctx);
      log.info("bye!");
    }

    if (datagramPacket.contains("NICE")) {

    }

    // 运行成功  置 map 中 lightStatus 为 lightStatus
    if (datagramPacket.contains("STAT")) { // SIG
      String long_id = circuitBoardService.findByCBIP(ip).getLongId();
      String[] stats = datagramPacket.split("STAT");
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
    } */
    }

/*   @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);

    String IP = (ctx.channel().remoteAddress().toString().split("/"))[1];
    log.info("电路板 : " + IP + " 状态：active !\n");
  }

  // 失去连接的动作
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    // 移除MAP和DB里的这个连接
    String IP = (ctx.channel().remoteAddress().toString().split("/"))[1];
    CircuitBoard circuitBoard = circuitBoardService.findByCBIP(IP);
    if (Validator.isNotNull(circuitBoard)) {
      String longId = circuitBoard.getLongId();
      NettySocketHolder.remove(longId);
      redisUtils.del(CommonParams.REDIS_BOARD_SERVER_PREFIX + longId);
      circuitBoardService.deleteById(circuitBoard);
      log.error("板卡" + longId + "失去连接，已从map和db中移除");
    }
    // 链路关闭
    ctx.channel().close();
    log.error("Channel is disconnected");
  } */

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    /*************************************处理Map和DB***************************************/

    //初始化以及更新NettySocketHolder(Map)和数据库中的电路板记录(仅当电路板Login时)
    private HashMap<String, Object> initMap(ChannelHandlerContext ctx, String IP, String long_id) {
        HashMap<String, Object> info = new HashMap<>();
        info.put("ip", IP);
        info.put("ctx", ctx);
        info.put("status", "0");
        info.put("isRecorded", "0");
        info.put("buttonStatus", "");
        info.put("lightStatus", "");
        info.put("nixieTubeStatus", "");
        info.put("longId", long_id);
        return info;
    }

    private CircuitBoard initCBDBObj(String longId, String IP) {
        CircuitBoard board = new CircuitBoard();
        board.setLongId(longId);
        board.setCbIp(IP);
        board.setStatus("0");
        board.setIsRecorded("0");
        board.setLightStatus("");
        board.setSwitchButtonStatus("");
        return board;
    }

    private void initCBDBObjWithIdKnown(String longId, String IP, CircuitBoard board) {
        board.setLongId(longId);
        board.setCbIp(IP);
        board.setStatus("0");
        board.setIsRecorded("0");
        board.setLightStatus("");
        board.setSwitchButtonStatus("");
    }

    private void insertNewTOMap(ChannelHandlerContext ctx, String long_id) {
        String IP = (ctx.channel().remoteAddress().toString().split("/"))[1];
        //新的电路板信息
        HashMap<String, Object> info = initMap(ctx, IP, long_id);
        // keypoint 根据 long_id 去 map 中找，如果找到了，map 更新，所有状态复原； 如果没找到，在 map 中添加。
        // 内容加入单例 Map  long_id : info
        if (NettySocketHolder.getInfo(long_id) != null)
            log.info("map中已有 " + long_id + " ，更新map");
        else
            log.info("添加新 CB 到 map！ longId: " + long_id + " remoteAddress : " + IP);
        NettySocketHolder.getInstance().put(long_id, info);
    }

    private void insertNewTODataBase(ChannelHandlerContext ctx, String longId) {
        // 根据 longId 去数据库中找，如果找到了，数据库更新，所有状态复原； 如果没找到，在数据库中添加。
        String IP = (ctx.channel().remoteAddress().toString().split("/"))[1];

        // 更新数据库
        CircuitBoard circuitBoard = circuitBoardService.findByCBID(longId);
        if (circuitBoard != null) {
            initCBDBObjWithIdKnown(longId, IP, circuitBoard);
            circuitBoardService.updateById(circuitBoard);
            log.info("数据库更新了" + longId + "！");
        } else {
            //向数据库中添加这个电路板
            CircuitBoard obj = initCBDBObj(longId, IP);
            circuitBoardService.insert(obj);
            log.info("添加新CB 到 DB！longId: " + longId);
        }
    }

    //更新NettySocketHolder(Map)和数据库中的电路板记录(其他时候)
    private void updateMap(ChannelHandlerContext ctx, String long_id) {
        // 根据 long_id 去 map 中找，如果没找到，在 map 中添加。
        if (NettySocketHolder.getInfo(long_id) == null) {
            String IP = (ctx.channel().remoteAddress().toString().split("/"))[1];
            HashMap<String, Object> info = initMap(ctx, IP, long_id);
            //内容加入单例 Map
            updateDataBase(ctx, long_id);
            NettySocketHolder.getInstance().put(long_id, info);
            log.info("添加新 CB 到 map！ longId: " + long_id + " remoteAddress : " + IP);
        }
    }

    private void updateDataBase(ChannelHandlerContext ctx, String longId) {
        // 根据 longId 去数据库中找，如果找到了，数据库更新，所有状态复原； 如果没找到，在数据库中添加。
        // 更新数据库
        CircuitBoard circuitBoard = circuitBoardService.findByCBID(longId);
        if (circuitBoard == null) {
            //向数据库中添加这个电路板
            insertNewTODataBase(ctx, longId);
        }
    }
}
