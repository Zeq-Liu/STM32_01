package com.yunfd.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.service.*;
import com.yunfd.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class IdentifyServiceImpl implements IdentifyService {
  @Resource
  private RedisUtils redisUtils;

  @Autowired
  private CbUseTimeService cbUseTimeService;

  @Autowired
  private BoardOperationService boardOperationService;

  @Autowired
  private WaitingService waitingService;

  @Autowired
  private CircuitBoardService circuitBoardService;

  //删除所有信息以及删除用户的文件
  @Override
  public void clearUserRedisInfoAndSaveData(String token) {
    Object o = redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token);
    if (Validator.isNotNull(o)) {
      UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, o);
      boolean b = cbUseTimeService.saveUserUsingData(connectionVo);
      if (b) {
        String cbIpPort = connectionVo.getCbIpPort();
        try {
          circuitBoardService.simplyFreeCB(cbIpPort);
        } catch (Exception e) {
          e.printStackTrace();
        }
        redisUtils.del(CommonParams.REDIS_CONN_PREFIX + token, CommonParams.REDIS_TTL_PREFIX + token, CommonParams.REDIS_CONN_SHADOW_PREFIX + token, CommonParams.REDIS_OP_TTL_PREFIX + token);
        waitingService.outOfLine(token);
        log.info("用户使用信息已保存，redis记录已清除");
        boardOperationService.clearSteps(token);
        log.info("用户相关文件已清除");
      } else log.error("保存用户使用信息出错");
    }
  }

  //用于确认用户还在系统中
  @Override
  public boolean checkValidity(String token) {
    Object o = redisUtils.get(CommonParams.REDIS_TTL_PREFIX + token);
    return Validator.isNotNull(o);
  }
}
