package com.yunfd.domain;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author DJ
 */
@TableName("cb_use_time")
@Data
@EqualsAndHashCode(callSuper = false)
public class CbUseTime extends BaseModel<CbUseTime> {

  // 电路板longId
  private String cbLongId;

  private String cbIp;

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


  public CbUseTime(String cbIp) {
    this.cbIp = cbIp;
  }

  public CbUseTime() {
  }
}
