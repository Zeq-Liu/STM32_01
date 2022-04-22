package com.yunfd.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
  private String workId;
  private String school;
  private String platformTag;
  //用于防止他人使用的一个随机串
  private String salt;
  private String username;
}
