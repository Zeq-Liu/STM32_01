package com.yunfd.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.CircuitBoard;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.mapper.CircuitBoardMapper;
import com.yunfd.netty.NettySocketHolder;
import com.yunfd.service.CbUseTimeService;
import com.yunfd.service.CircuitBoardService;
import com.yunfd.service.IdentifyService;
import com.yunfd.util.RedisUtils;
import com.yunfd.util.core.SendMessageToCB;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * Created by DJ on 2018/7/8.
 */
@Service
@Slf4j
public class CircuitBoardServiceImpl extends ServiceImpl<CircuitBoardMapper, CircuitBoard> implements CircuitBoardService {

  @Autowired
  private CbUseTimeService cbUseTimeService;

  @Autowired
  private IdentifyService identifyService;

  @Autowired
  private RedisUtils redisUtils;

  // 用来获取 TCP/IP 传输 ctx
  private static NettySocketHolder nettySocketHolder;

  @Override
  public CircuitBoard findByCBIP(String ip) {
    List<CircuitBoard> list = baseMapper.selectList(new EntityWrapper<CircuitBoard>().eq("cb_ip", ip));
    if (list.size() > 0) return list.get(0);
    else return null;
  }

  @Override
  public CircuitBoard findByCBID(String longId) {
    List<CircuitBoard> list = baseMapper.selectList(new EntityWrapper<CircuitBoard>().eq("long_id", longId));
    if (list.size() > 0) return list.get(0);
    else return null;
  }

  @Override
  public CircuitBoard getAFreeBoard() {
    //找寻一块没有被预约的空闲板子
    List<CircuitBoard> list = baseMapper.selectList(new EntityWrapper<CircuitBoard>().eq("status", "0"));
    if (list.size() > 0) {
      //随机获取空闲board
      CircuitBoard board = list.get(RandomUtil.randomInt(list.size()));
      board.setStatus("1");
      baseMapper.updateById(board);
      return board;
    } else return null;
  }

  // 仅仅是释放板卡，并且保存用户信息
  // 还没有删除缓存中的用户数据和删除用户文件
  @Override
  public boolean freeCB(String token) {
    UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_SHADOW_PREFIX + token));
    String cbIpPort = connectionVo.getCbIpPort();
    simplyFreeCB(cbIpPort);
    if (Validator.isNotNull(connectionVo.getLongId())) {
//      //更新数据库,记录时长
//      CbUseTime time = null;
//      List<CbUseTime> list = cbUseTimeService.selectList(new EntityWrapper<CbUseTime>().eq("id", connectionVo.getBillId()));
//      if (list.size() > 0) time = list.get(0);
//      else {
//        time = new CbUseTime();
//        String[] split = token.split("_");
//        time.setId(connectionVo.getBillId());
//        time.setDuration(0);
//        time.setCreateTime(connectionVo.getCreateTime());
//        time.setPlatformTag(split[0]);
//        time.setSchoolName(split[1]);
//        time.setWorkId(split[2]);
//        time.setUserIp(connectionVo.getUserIp());
//        time.setCbIp(connectionVo.getCbIp());
//        time.setCbLongId(connectionVo.getLongId());
//      }
//      time.setDuration((time.getDuration() + (int) (CommonParams.REDIS_CONN_SHADOW_LIMIT - connectionVo.getLeftSeconds() + (now.getTime() - connectionVo.getUpdateTime().getTime()) / 1000) / 60));
//      cbUseTimeService.insertOrUpdate(time);
      identifyService.clearUserRedisInfoAndSaveData(token);
      log.info("已更新板卡" + connectionVo.getLongId() + "使用时长");
      return true;
    }
    return false;
  }

  //仅仅是释放一块板子而已，同时更新数据库
  @Override
  public String simplyFreeCB(String cbIp) {
    //正则匹配，检查有效性
    // final String reg = "^(\\d+\\.){3}\\d+:\\d+$";
    final String reg = "^(\\d+\\.){3}\\d+$";
    boolean match = ReUtil.isMatch(reg, cbIp);

    if (match) {
      List<CircuitBoard> selectList = baseMapper.selectList(new EntityWrapper<CircuitBoard>().eq("cb_ip", cbIp));
      if (selectList.size() > 0) {
        for (CircuitBoard board : selectList) {
          String longId = board.getLongId();
          ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
          SendMessageToCB.sendENDToCB(ctx,NettySocketHolder.getInstance().getSocketAddress(longId));

          //更新map
          HashMap<String, Object> info = NettySocketHolder.getInfo(longId);
          NettySocketHolder.remove(longId);
          if (Validator.isNull(info)) info = new HashMap<>();
          info.put("status", "0");
          info.put("isRecorded", "0");
          info.remove("filePath");
          info.remove("count");
          info.put("buttonStatus", "");
          info.put("lightStatus", "");
          NettySocketHolder.put(longId, info);

          //更新数据库
          board.setStatus("0");
          baseMapper.updateById(board);
          log.info("板卡" + board.getLongId() + "已释放");
        }
      }
      return cbIp;
    }
    return null;
  }
}
