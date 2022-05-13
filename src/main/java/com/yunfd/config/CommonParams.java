package com.yunfd.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import com.yunfd.domain.vo.UserVo;

import java.util.Arrays;

public class CommonParams {
  @Deprecated
  public final static String fileBaseRelativeURL = "yunfd/upload/";

  @Deprecated
  public final static String bitFileStaticName = "vote5v1_1";

  public final static int sliceSize = 1024;
  public final static String[] authorizedPlatforms = {"bigData", "interrupt"};

  //保持连接 身份->true
  public final static String REDIS_TTL_PREFIX = "ttl:";
  public final static Integer REDIS_TTL_LIMIT = 3 * 60; //connection must be checked within 3 minutes
  //保持操作 身份->true
  public final static String REDIS_OP_TTL_PREFIX = "op:";
  public final static Integer REDIS_OP_TTL_LIMIT = 3 * 60;
  //板卡使用计时器 身份->conn_obj
  public final static String REDIS_CONN_PREFIX = "conn:";
  //板卡使用计时器的shadow,单纯的倒计时器
  public final static String REDIS_CONN_SHADOW_PREFIX = "shadow:";
  public final static Integer REDIS_CONN_SHADOW_LIMIT = 30 * 60;

  //板卡连接服务器的倒计时
  public final static String REDIS_BOARD_SERVER_PREFIX = "boardConnection:";
  public final static Integer REDIS_BOARD_SERVER_LIMIT = 2 * 60 + 30;

  public final static String USER_IP_HEADER = "X-real-ip";


  public static String getBase() {
    String base = "yunfd/operation/";
    String absolutePath = FileUtil.getAbsolutePath(base);
    if (absolutePath.contains("target/")) return "../../" + base;
    else return base;
  }

  public static String getFullPath(String fileName) {
    String opFileSuffix = ".txt";
    return FileUtil.getAbsolutePath(getBase() + fileName + opFileSuffix);
  }

  public static String getBitFileBase() {
    String base = "yunfd/upload/";
    String absolutePath = FileUtil.getAbsolutePath(base);
    if (absolutePath.contains("target/")) return "../../" + base;
    else return base;
  }

  public static String getFullBitFilePath(String fileName) {
    String bitFileSuffix = ".bit";
    return FileUtil.getAbsolutePath(getBitFileBase() + fileName + bitFileSuffix);
  }

  //格式不对则返回null
  public static String generateUserToken(UserVo userVo) {
    String platformTag = userVo.getPlatformTag();
    String userSchool = userVo.getSchool();
    String userWorkId = userVo.getWorkId();
    String salt = userVo.getSalt();

    if (isLegalParamsWithoutSalt(userVo) && Validator.isNotNull(salt) && !salt.equals("")) {
      return String.join("_", platformTag, userSchool, userWorkId, salt);
    }
    return null;
  }

  public static boolean isLegalParamsWithoutSalt(UserVo userVo) {
    String platformTag = userVo.getPlatformTag();
    String userSchool = userVo.getSchool();
    String userWorkId = userVo.getWorkId();

    if (Validator.isNotNull(platformTag) && Arrays.binarySearch(authorizedPlatforms, platformTag) >= 0) {
      return Validator.isNotNull(userSchool) && Validator.isNotNull(userWorkId)
              && !userSchool.equals("") && !userWorkId.equals("");
    }
    return false;
  }
}
