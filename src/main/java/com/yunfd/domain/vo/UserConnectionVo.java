package com.yunfd.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 存储于Redis中的板卡连接信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserConnectionVo {
  private String token;
  private Date createTime;
  private Date updateTime;
  private Long leftSeconds;
  private boolean isFrozen;
  //操作流水号，用于记录用户的使用情况
  private String billId;
  private String userIp;
  private String cbIpPort;
  private String longId;
}
