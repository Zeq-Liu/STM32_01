package com.yunfd.domain;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.*;

/**
 * Created by DJ on 2018/7/8.
 */

@TableName("cb")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CircuitBoard extends BaseModel<CircuitBoard> {

  // 有用

  private String cbIp;                    //32
  //是否被占用
  private String status;
  //是否被预定
  private String isReserved;
  private String isRecorded;

  // 存储板子编号
  private String longId;                  //50

  // 无用

  private String lightStatus;             //32
  private String switchButtonStatus;      //todo 准备删除
  private String tapButtonStatus;         //todo 准备删除
  private String isVideo;                 //todo 准备删除

  public CircuitBoard(String cb_ip) {
    this.cbIp = cbIp;
  }
}
