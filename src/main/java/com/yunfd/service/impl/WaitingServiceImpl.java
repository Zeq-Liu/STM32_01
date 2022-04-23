package com.yunfd.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.CircuitBoard;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.service.BoardOperationService;
import com.yunfd.service.WaitingService;
import com.yunfd.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class WaitingServiceImpl implements WaitingService {
  @Resource
  private RedisTemplate<String, Object> redisTemplate;
  @Resource
  private RedisUtils redisUtils;
  @Autowired
  private BoardOperationService boardOperationService;

  @Value("${userRedis.lineName}")
  private String lineName;

  @Value("${userMaximumConnectionTime}")
  private Long userMaximumConnectionTime;

  @Override
  public Long getNumberOfPeopleInFrontOfUser(String userToken) {
    return redisTemplate.opsForZSet().rank(lineName, userToken);
  }

  @Override
  public boolean waitInLine(String userToken) {
    return redisTemplate.opsForZSet().add(lineName, userToken, getTotalWaitingNum());
  }

  @Override
  public Long outOfLine(String userToken) {
    return redisTemplate.opsForZSet().remove(lineName, userToken);
  }

  @Override
  public Long getTotalWaitingNum() {
    return redisTemplate.opsForZSet().zCard(lineName);
  }

  //用户时长
  @Override
  public UserConnectionVo getConnectionObj(String userToken) {
    Object o = redisUtils.get(CommonParams.REDIS_CONN_PREFIX + userToken);
    return Convert.convert(UserConnectionVo.class, o);
  }

  @Override
  public UserConnectionVo createConnectionObj(HttpServletRequest request, String token) {
    UserConnectionVo userConnectionVo = getConnectionObj(token);
    if (Validator.isNull(userConnectionVo)) {
      userConnectionVo = new UserConnectionVo();
      Date now = new Date();
      userConnectionVo.setCreateTime(now);
      userConnectionVo.setFrozen(true);
      userConnectionVo.setUpdateTime(now);
      userConnectionVo.setToken(token);
      userConnectionVo.setLeftSeconds((long) CommonParams.REDIS_CONN_SHADOW_LIMIT);
      userConnectionVo.setUserIp(request.getHeader(CommonParams.USER_IP_HEADER));
    }
    return userConnectionVo;
  }

  @Override
  public boolean freezeConnection(String userToken) {
    try {
      Object shadow = redisUtils.get(CommonParams.REDIS_CONN_SHADOW_PREFIX + userToken);
      Object o = redisUtils.get(CommonParams.REDIS_CONN_PREFIX + userToken);
      UserConnectionVo vo = Convert.convert(UserConnectionVo.class, o);
      if (Validator.isNotNull(shadow) && Validator.isNotNull(vo) && !vo.isFrozen()) {
        vo.setFrozen(true);
        vo.setLeftSeconds(vo.getLeftSeconds() - (new Date().getTime() - vo.getUpdateTime().getTime()) / 1000);
        vo.setUpdateTime(new Date());
        //不过期
        redisUtils.set(CommonParams.REDIS_CONN_SHADOW_PREFIX + userToken, true, -1);
        redisUtils.set(CommonParams.REDIS_CONN_PREFIX + userToken, vo, -1);
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  //有空闲电路板并且已经排到
  @Override
  public Long unfreezeConnection(String userToken, CircuitBoard board) {
    Object o = redisUtils.get(CommonParams.REDIS_CONN_PREFIX + userToken);
    Object shadow = redisUtils.get(CommonParams.REDIS_CONN_SHADOW_PREFIX + userToken);
    UserConnectionVo vo = Convert.convert(UserConnectionVo.class, o);
    if (Validator.isNotNull(shadow) && Validator.isNotNull(vo) && vo.isFrozen()) {
      outOfLine(userToken); // 其实应该直接弹出头部
      vo.setFrozen(false);
      vo.setCbIp(board.getCbIp());
      vo.setLongId(board.getLongId());
      vo.setUpdateTime(new Date());

      //恢复环境 (不用在这里，而是在sysFile里面的reload里面实现)
      // boolean b = boardOperationService.reloadEnv(userToken, board.getLongId());

      //开始计时
      redisUtils.set(CommonParams.REDIS_CONN_PREFIX + userToken, vo);
      redisUtils.set(CommonParams.REDIS_CONN_SHADOW_PREFIX + userToken, true, vo.getLeftSeconds());
      redisUtils.set(CommonParams.REDIS_OP_TTL_PREFIX + userToken, true, CommonParams.REDIS_OP_TTL_LIMIT);
      return vo.getLeftSeconds();
    } else return 0L;
  }
}
