package com.yunfd.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.netty.NettySocketHolder;
import com.yunfd.service.BoardOperationService;
import com.yunfd.util.RedisUtils;
import com.yunfd.util.SendMessageToCB;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
//处理用户的操作信息，持久化
public class BoardOperationServiceImpl implements BoardOperationService {

  @Resource
  private RedisUtils redisUtils;

  @Override
  public boolean initOperationList(String userInfo) {
    try {
      String fileFullPath = CommonParams.getFullPath(userInfo);
      if (FileUtil.exist(fileFullPath)) FileUtil.del(fileFullPath);
      FileUtil.touch(fileFullPath);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean appendStepsToList(String userInfo, List<String> steps) {
    try {
      String fullPath = CommonParams.getFullPath(userInfo);
      FileUtil.appendUtf8Lines(steps, fullPath);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean appendAStepToList(String userInfo, String step) {
    try {
      String fullPath = CommonParams.getFullPath(userInfo);
      FileUtil.appendUtf8String(step + "\n", fullPath);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public List<String> readSteps(String userInfo) {
    try {
      String fullPath = CommonParams.getFullPath(userInfo);
      return FileUtil.readUtf8Lines(fullPath);
    } catch (Exception e) {
      log.info("读取操作步骤出错，可能没有这个文件");
      return new ArrayList<>();
    }
  }

  @Override
  public void clearSteps(String userInfo) {
    FileUtil.del(CommonParams.getFullPath(userInfo));
    FileUtil.del(CommonParams.getFullBitFilePath(userInfo));
    log.info("已清空用户文件信息");
  }

  @Override
  public boolean reloadEnv(String userInfo, String longId) {
    if (FileUtil.exist(CommonParams.getFullBitFilePath(userInfo))) {
      HashMap<String, Object> info = NettySocketHolder.getInfo(longId);
      ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);

      String filePath = CommonParams.getFullBitFilePath(userInfo);

      info.put("isRecorded", "0");
      info.put("count", 0);
      info.put("filePath", filePath);
      NettySocketHolder.put(longId, info);
      log.info("instance:  " + NettySocketHolder.getInfo(longId));
      //烧录
      SendMessageToCB.recordBitOnCB(ctx, filePath, 0);
      return true;
    }
    return false;
  }

  @Override
  public void recordBitFileToBoardForTheFirstTime(String token, String filePath) {
    Object o = redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token);
    UserConnectionVo vo = Convert.convert(UserConnectionVo.class, o);
    String longId = vo.getLongId();

    if (Validator.isNotNull(longId) && !longId.equals("")) {
      ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
      HashMap<String, Object> info = NettySocketHolder.getInfo(longId);
      info.put("isRecorded", "0");
      info.put("count", 0);
      info.put("filePath", filePath);
      NettySocketHolder.put(longId, info);
      log.info("instance:  " + NettySocketHolder.getInfo(longId));
      SendMessageToCB.recordBitOnCB(ctx, filePath, 0);

    }
  }
}
