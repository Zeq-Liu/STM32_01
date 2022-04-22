package com.yunfd.config;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.yunfd.domain.CircuitBoard;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.netty.NettySocketHolder;
import com.yunfd.service.CircuitBoardService;
import com.yunfd.service.IdentifyService;
import com.yunfd.service.WaitingService;
import com.yunfd.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
  public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
    super(listenerContainer);
  }

  @Value("${server.port}")
  private String port;

  @Autowired
  private IdentifyService identifyService;
  @Autowired
  private WaitingService waitingService;
  @Autowired
  private CircuitBoardService circuitBoardService;
  @Autowired
  private RedisUtils redisUtils;

  /**
   * 处理过期的key(ttl,shadow)
   *
   * @param message 过期的key
   */
  @Override
  public void onMessage(Message message, byte[] pattern) {
    String expiredKey = message.toString();
    try {
      String[] split = expiredKey.split(":");
      String token = split[1];
      //清除无连接的板卡
      if (CommonParams.REDIS_BOARD_SERVER_PREFIX.contains(split[0])) {
        String boardLongId = token;
        NettySocketHolder.remove(boardLongId);
        boolean b = circuitBoardService.delete(new EntityWrapper<CircuitBoard>().eq("long_id", boardLongId));
        log.error("板卡" + boardLongId + "失去连接，已从map和db中移除");
      }
      //释放长时间无操作的板卡
      else if (CommonParams.REDIS_OP_TTL_PREFIX.contains(split[0])) {
        //用于计时3分钟内的操作，过期释放板卡保存操作
        UserConnectionVo vo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
        circuitBoardService.simplyFreeCB(vo.getCbIp());
        waitingService.freezeConnection(token);
      }
      //结束实验保存数据
      else {
        identifyService.clearUserRedisInfoAndSaveData(token);
      }
      log.info("token expired: " + token);
    } catch (Exception e) {
      log.info(expiredKey + " 有key过期了，但是没有成功执行监听方法");
    }
  }
}
