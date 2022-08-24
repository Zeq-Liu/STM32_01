package com.yunfd.domain;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据库类--使用板卡与相关信息
 * @Description
 * @Author LiuZequan
 * @Date 2022/6/17 11:45
 * @Version 2.0
 */

@TableName("cb_use_time")
@Data
@EqualsAndHashCode(callSuper = false)
public class CbUseTime extends BaseModel<CbUseTime> {

  // 电路板longId
  private String cbLongId;

  private String cbIpPort;

  private String schoolName;

  private String workId;

  private String platformTag;

  private String username;

  private String userIp;
  // 实验时长（min)
  private int duration;
  // 文件上传次数
  private int fileUploadNum;

  private String moreInfo;


  public CbUseTime(String cbIpPort) {
    this.cbIpPort = cbIpPort;
  }

  public CbUseTime() {
  }
}
