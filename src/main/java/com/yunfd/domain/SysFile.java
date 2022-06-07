package com.yunfd.domain;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("sys_file")
@Data
@EqualsAndHashCode(callSuper = false)
public class SysFile extends BaseModel<SysFile> {

  private String fileOriginname;   //文件原始名称
  private String fileName;   //标准化文件名
  private String fileType;   //文件类型  区分是图片还是其他类型文件
  private String fileCtype;  //如果是图片 ，那么具体的图片类型 是什么  或是  其他类型文件的话  具体文件类型是什么
  private String fileIntroduce;    //文件介绍
  private String DirectfilePath;      //文件绝对存放路径
  private String ResourceloadPath;     //文件展示路径

  public SysFile(String fileName) {
    this.fileName = fileName;
  }

  public SysFile() {
  }
}
