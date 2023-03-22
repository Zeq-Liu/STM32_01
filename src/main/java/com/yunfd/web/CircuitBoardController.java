package com.yunfd.web;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.CircuitBoard;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.netty.NettySocketHolder;
import com.yunfd.service.*;
import com.yunfd.util.RedisUtils;
import com.yunfd.util.core.SendMessageToCB;
import com.yunfd.web.vo.ResultVO;
import io.netty.channel.ChannelHandlerContext;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.InetSocketAddress;
import java.util.*;

import static com.yunfd.util.core.SendMessageToCB.sendInitToCB;

/**
 * @Description
 * @Date 2022/6/8 23:39
 * @Version 1.0
 */

@RestController
@RequestMapping("/cb")
@Slf4j
public class CircuitBoardController extends BaseController<CircuitBoardService, CircuitBoard> {

    @Autowired
    private CircuitBoardService circuitBoardService;

    @Autowired
    private CbUseTimeService cbUseTimeService;

    @Autowired
    private BoardOperationService boardOperationService;

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private IdentifyService identifyService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 用户主动释放板卡
     * @param request
     * @return {
     *      "code": 0,
     *      "msg": "已释放板卡",
     *      "result": null
     * }
     */
    @ApiOperation("用户主动释放板卡，清除redis、文件，保存用户使用信息")
    @PostMapping("/freeCB")
    public ResultVO freeCB(HttpServletRequest request) {
        String token = request.getHeader("token");
        identifyService.clearUserRedisInfoAndSaveData(token);
        log.info("已释放板卡");
        return ResultVO.ok("已释放板卡");
    }

    /**
     * 通过longId查询电路板是否已烧录，msg返回 0，表示未烧录，返回 1，表示烧录
     * @param longId
     * @return {
     *     "code": 0,
     *     "msg": "0",
     *     "result": null
     * }
     */
    @ApiOperation("查询电路板是否已烧录,使用longId，他们都是唯一的")
    @PostMapping("/getRecordedStatusByLongId")
    public ResultVO getRecordedStatusByLongId(@RequestParam("longId") String longId) {
        CircuitBoard board;

        board = circuitBoardService.selectOne(new EntityWrapper<CircuitBoard>().eq("long_id", longId));

        if (board == null) return ResultVO.error("没有这个板子");

        if (!NettySocketHolder.getInfo(board.getLongId()).get("isRecorded").toString().equals("")) {
            String result = NettySocketHolder.getInfo(board.getLongId()).get("isRecorded").toString();
            log.info("isRecorded not null:  isRecorded: " + result);
            return ResultVO.ok(result);
        } else {
            return ResultVO.error("isRecorded为空");
        }
    }

    @ApiOperation("查询电路板是否已烧录,使用ipPort，他们都是唯一的")
    @PostMapping("/getRecordedStatusByCbIpPort")
    public ResultVO getRecordedStatusByCbIpPort(@RequestParam("cbIpPort") String cbIpPort) {
        CircuitBoard board;

        board = circuitBoardService.selectOne(new EntityWrapper<CircuitBoard>().eq("cb_ip_port", cbIpPort));

        if (board == null) return ResultVO.error("没有这个板子");

        if (!NettySocketHolder.getInfo(board.getLongId()).get("isRecorded").toString().equals("")) {
            String result = NettySocketHolder.getInfo(board.getLongId()).get("isRecorded").toString();
            log.info("isRecorded not null:  isRecorded: " + result);
            return ResultVO.ok(result);
        } else {
            return ResultVO.error("isRecorded为空");
        }
    }


    //
    //  /**
    //   * 更改电路板烧录状态
    //   * @param cbIp
    //   * @param isRecord
    //   * @return
    //   */
    //  @ApiOperation("更改电路板烧录状态")
    //  @PostMapping("/setRecordedStatus")
    //  public ResultVO setRecordedStatus(@RequestParam String cbIp, @RequestParam String isRecord) {
    //    String result = "没有找到记录的isRecorded！";
    //    CircuitBoard circuitBoard = null;
    //    try {
    //      circuitBoard = circuitBoardService.findByCBIP(cbIp);
    //    } catch (Exception e) {
    //      System.out.println(result);
    //      return ResultVO.error(result);
    //    }
    //
    //    String longId = circuitBoard.getLongId();
    //
    //    try {
    //      if (NettySocketHolder.getInfo(longId).get("isRecorded").toString() != "") {//非空
    //        HashMap<String, Object> newInfo = NettySocketHolder.getInfo(longId);
    //        newInfo.remove("isRecorded");
    //        newInfo.put("isRecorded", isRecord);
    //        NettySocketHolder.remove(longId);
    //        NettySocketHolder.put(longId, newInfo);
    //        System.out.println("isRecorded not null:  isRecorded: " + result);
    //        return ResultVO.ok();
    //      } else {
    //        return ResultVO.error(result);
    //      }
    //    } catch (NullPointerException e) {
    //      System.out.println("error!");// 没有这个 Instance
    //      System.out.println("current instance: " + NettySocketHolder.getInstance().toString());
    //    }
    //    return ResultVO.ok();
    //  }
    //

    /**
     * 接受按钮状态，并发送灯状态，实时保存操作到文件中
     *
     * @param buttonStatus
     * @return{
     *     "code": 0,
     *     "msg": "按钮状态已发送！",
     *     "result": null
     * }
     */
    @ApiOperation("前端发送16位按钮状态键串，要求：需发送16个按键被点击的状态，0表示未点击过，1表示点击过，按板卡从左到右，上到下的顺序")
    @PostMapping("/sendButtonString")
    public ResultVO sendButtonString(HttpServletRequest request, @RequestParam("buttonStatus") String buttonStatus) {
        String token = request.getHeader("token");
        UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
        String longId = connectionVo.getLongId();

        System.out.println("buttonStatus " + buttonStatus);

        // String finalString = SendMessageToCB.ProcessButtonString(buttonStatus);
        //按钮字符串存入Map
        HashMap<String, Object> info = NettySocketHolder.getInfo(longId);
        info.replace("buttonStatus", buttonStatus);

        //更新操作计时器 plus:烧录文件也应该要更新计时器
        redisUtils.set(CommonParams.REDIS_OP_TTL_PREFIX + token, true, CommonParams.REDIS_OP_TTL_LIMIT);

        //要在bin文件烧录完成后才能传输
        System.out.println("*********************************************"+NettySocketHolder.getInfo(longId).get("isRecorded").toString()+"*****************************");
        if (NettySocketHolder.getInfo(longId).get("isRecorded").toString().equals("1")) {
            //发送按钮状态
            SendMessageToCB.sendButtonStringToCB(NettySocketHolder.getInstance().getCtx(longId), NettySocketHolder.getInstance().getSocketAddress(longId), buttonStatus);

            //按钮字符串存入服务器指定位置
            //追加处理过的字符串
            boardOperationService.appendAStepToList(token, buttonStatus);

            log.info("按钮状态已发送！");
            return ResultVO.ok("按钮状态已发送！");
        } else {
            return ResultVO.error("bin文件没有烧录好！");
        }
    }

    /**
     * 获取 NixieTube String，格式如下
     * @param request
     * @return{
     *     "code": 0,
     *     "msg": "0111",
     *     "result": null
     * }
     */
    @ApiOperation("获取数码管状态")
    @PostMapping("/getNixieTubeString")
    public ResultVO getNixieTubeString(HttpServletRequest request) {
        String token = request.getHeader("token");
        UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
        String longId = connectionVo.getLongId();
        // 打印id
        System.out.println("longId: " + longId);
        try {
            String nixieTubeString = NettySocketHolder.getInfo(longId).get("nixieTubeStatus").toString();
            return ResultVO.ok(nixieTubeString);
        } catch (NullPointerException e) {
            return ResultVO.error("nixieTubeStatus is null");
        }
    }

    /**
     * 获取 Light String
     */
    @ApiOperation("获取LED状态")
    @PostMapping("/getLightString")
    public ResultVO getLightString(HttpServletRequest request) {
        String token = request.getHeader("token");
        UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
        String longId = connectionVo.getLongId();
        // 打印id
        System.out.println("longId: " + longId);
        try {
            String lightString = NettySocketHolder.getInfo(longId).get("lightStatus").toString();
            System.out.println("lightStatus："+lightString);
            return ResultVO.ok(lightString);
        } catch (NullPointerException e) {
            return ResultVO.error("lightStatus is null");
        }
    }

    /**
     * 获取 displayScreen String
     */
    @ApiOperation("获取显示屏状态")
    @PostMapping("/getDisplayScreenString")
    public ResultVO getDisplayScreenString(HttpServletRequest request) {
        String token = request.getHeader("token");
        UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
        String longId = connectionVo.getLongId();
        // 打印id
        System.out.println("longId: " + longId);
        try {
            String displayScreenString = NettySocketHolder.getInfo(longId).get("screenStatus").toString();
            return ResultVO.ok(displayScreenString);
        } catch (NullPointerException e) {
            return ResultVO.error("screenStatus is null");
        }
    }


    /**
     * 获取 处理过的btnStr
     */
    @ApiOperation("获取键串")
    @PostMapping("/getProcessedBtnStr")
    // 刚刚发送的键串，可能有用
    public ResultVO getProcessedBtnStr(HttpServletRequest request) {
        String token = request.getHeader("token");
        UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
        String longId = connectionVo.getLongId();
        try {
            String buttonStatus = NettySocketHolder.getInfo(longId).get("buttonStatus").toString();
            return ResultVO.ok(buttonStatus);
        } catch (NullPointerException e) {
            return ResultVO.error("buttonStatus is null");
        }
    }

    @ApiOperation("重置板卡状态/点击reset按钮")
    @PostMapping("/reset")
    public ResultVO reset(HttpServletRequest request) {
        String token = request.getHeader("token");
        UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
        String longId = connectionVo.getLongId();
        try {
            ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
            HashMap<String, Object> info = NettySocketHolder.getInfo(longId);
            String ipPort = (String) info.get("ipPort");
            InetSocketAddress socketAddress = new InetSocketAddress(ipPort.split(":")[0], Integer.parseInt(ipPort.split(":")[1]));
            //将缓冲map中的isRecord设置为0
            info.replace("isRecorded","0");

            sendInitToCB(ctx, socketAddress);

            return ResultVO.ok("状态重置已完成");
        } catch (NullPointerException e) {
            return ResultVO.error("状态重置失败");
        }
    }

    // @ApiOperation("载入操作的记录操作：tag为0或1，为0表示清空记录，为1表示载入记录")
    // @PostMapping("/loadHistory")
    // public ResultVO loadHistory(HttpServletRequest request, @RequestParam("tag") String tag) {
    //     String token = request.getHeader("token");
    //
    //     if (tag.equals("0")) {
    //         //不载入历史，直接清空记录
    //         boardOperationService.clearSteps(token);
    //         return ResultVO.ok("历史清除成功");
    //     } else if (tag.equals("1")) {
    //         //此前，用户需先调用烧录板卡的方法!!!
    //         //载入历史
    //         UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
    //         String longId = connectionVo.getLongId();
    //         ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
    //         try {
    //             List<String> steps = boardOperationService.readSteps(token);
    //             for (String step : steps) {
    //                 //刷新一下操作计时器
    //                 redisUtils.set(CommonParams.REDIS_TTL_PREFIX + token, true, CommonParams.REDIS_TTL_LIMIT);
    //                 //传送步骤到board上面
    //                 SendMessageToCB.sendButtonStringToCB(ctx, NettySocketHolder.getInstance().getSocketAddress(longId), step);
    //             }
    //             return ResultVO.ok("载入成功");
    //         } catch (NullPointerException e) {
    //             return ResultVO.error("buttonStatus is null");
    //         }
    //     }
    //     return ResultVO.error();
    // }

  /*
  TODO 仅供测试
   */
    // @ApiOperation("往板卡发送消息")
    // @GetMapping("/test/sendMsg")
    // public void sendMsg(@RequestParam("longId") String longId, @RequestParam("msg") String msg) {
    //   ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
    //   ctx.channel().writeAndFlush(msg);
    // }
    //
    // @ApiOperation("获取map信息")
    // @GetMapping("/test/getMapInfo")
    // public ResultVO getMapInfo(@RequestParam("longId") String longId) {
    //   HashMap<String, Object> info = NettySocketHolder.getInfo(longId);
    //   return ResultVO.ok(info);
    // }
    //
    // @ApiOperation("获取ctx信息")
    // @GetMapping("/test/getCtx")
    // public ResultVO getCtx(@RequestParam("longId") String longId) {
    //   ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
    //   return ResultVO.ok(ctx);
    // }
    //
    // @ApiOperation("判断ctx信息")
    // @GetMapping("/test/testCtx")
    // public ResultVO testCtx(@RequestParam("longId") String longId, @RequestParam("value") boolean value) {
    //   ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
    //   return ResultVO.ok(ctx.isRemoved() == !value);
    // }
}