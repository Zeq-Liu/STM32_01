package com.yunfd.util;

import cn.hutool.core.io.file.FileNameUtil;
import com.yunfd.HduFPGAJavaApplication;
import com.yunfd.config.CommonParams;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SendMessageToCB {

  /**
   * 烧录过程  在bit文件传到位后进行调用
   */

  public static void recordBitOnCB(ChannelHandlerContext ctx, String BIT_FILE_PATH, int count) {
    log.info("烧录文件位置：" + BIT_FILE_PATH);
    byte[] b = ByteToFileUtil.getBytes(BIT_FILE_PATH);

    double limit = Math.ceil(b.length / (CommonParams.sliceSize + 0.0));
    //todo 有优化空间......

    //keypoint 更新操作计时器
    String token = FileNameUtil.mainName(BIT_FILE_PATH);
    //获取redis操作工具
    RedisUtils redisUtils = HduFPGAJavaApplication.ac.getBean(RedisUtils.class);
    redisUtils.set(CommonParams.REDIS_OP_TTL_PREFIX + token, true, CommonParams.REDIS_OP_TTL_LIMIT);

    //存储一个包一个包（12800/一个包）
    List<String> stringList = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      int length = CommonParams.sliceSize;
      if (i + 1 >= limit) {
        length = b.length % CommonParams.sliceSize;
      }
      byte[] dest = new byte[CommonParams.sliceSize];
      //截取数组
      System.arraycopy(b, i * CommonParams.sliceSize, dest, 0, length);
      stringList.add(ByteToFileUtil.bytesToHexString(dest)); // 存入链表
    }

    //发送到板卡 第0次和第1次数据发送有其他意思
    if (count == 0) {
      ctx.channel().writeAndFlush("NNN");
      ctx.channel().writeAndFlush("CALL 1234 #");
      log.info("the first time to send message");
    } else if (count == 1) {
      // b.length 文件字节数  传输前置
      int size = b.length / CommonParams.sliceSize;
      System.out.println("send size " + size);
      StringBuilder sizeString = new StringBuilder(Integer.toHexString(size));
      for (int i = sizeString.length(); i < 3; i++) {
        sizeString.insert(0, "0");
      }
      System.out.println("16进制的文件长度：" + sizeString);
      // 文件长度 * 2
      int c = b.length * 2;
      StringBuilder sizeString2 = new StringBuilder(Integer.toHexString(c));
      for (int i = sizeString2.length(); i < 6; i++) {
        sizeString2.insert(0, "0");
      }
      System.out.println("双倍16进制的文件长度：" + sizeString2);
      sizeString.append(sizeString2);
      ctx.channel().writeAndFlush("SIZE#" + sizeString + "#");
    } else if (count < limit + 2) {
      // int x = Integer.parseInt(count - 1 + "", 16) * 2 + 20;
      // String size = Integer.toHexString(x);
      // System.out.println("the " + count + " time to send message " + size);
      ctx.channel().writeAndFlush("FIL" + "FF" + stringList.get(count - 2));
      System.out.println("第 " + count + " 次发送数据 " + stringList.get(count - 2).length() * 2);
    } else {
      System.out.println("烧录完毕");
    }
  }

  /**
   * @param ctx
   * @param BUTTON_STRING
   */
  public static void sendButtonStringToCB(ChannelHandlerContext ctx, String BUTTON_STRING) {
    // NNN 是用户结束 和超时断线用的  "NNN #" + BUTTON_STRING + "#"
    System.out.println("CTR #" + BUTTON_STRING + "#");
    ctx.channel().writeAndFlush("CTR #" + BUTTON_STRING + "#");
  }

  /**
   * @param ctx
   */
  public static void sendENDToCB(ChannelHandlerContext ctx) {
    // 释放板子
    ctx.channel().writeAndFlush("NNN");
  }


  /**
   * 处理按钮字符串
   *
   * @param switchButtonStatus
   * @param tapButtonStatus
   * @return
   */
  public static String ProcessButtonString(String switchButtonStatus, String tapButtonStatus) {


    // String finalString = "";
    //
    // // 处理拨动开关按钮状态
    // int x;
    // // y -> 前8位，表示switchButtonStatus
    // StringBuilder y = new StringBuilder();
    // if (switchButtonStatus.length() > 32) {
    //   System.out.println("超长: " + switchButtonStatus.length());
    // } else if (switchButtonStatus.length() == 32) {
    //   for (int i = 0; i < 8; i++) {
    //     String tem = switchButtonStatus.substring(4 * i, 4 * (i + 1));
    //     // 二进制转十进制 Integer.valueOf("0101",2).toString()
    //     x = Integer.valueOf(tem, 2);
    //     // 十进制转成十六进制： Integer.toHexString(int i)
    //     y.append(Integer.toHexString(x));
    //   }
    //   System.out.println("前8位：" + y);
    // } else {
    //   System.out.println("switchButtonStatus 长度缺失: " + switchButtonStatus.length());
    // }
    //
    // /*
    //  * tapbutton处理
    //  *
    //  * 6位二进制转2位16进制
    //  * */
    // //t 末尾补两个0 --> 10110000  176  b0      10011000  152  98     11101100  236  ec
    // int m;
    // StringBuilder n = new StringBuilder();
    // if (tapButtonStatus.length() == 6) {
    //   String tap = tapButtonStatus + "00";
    //   for (int i = 0; i < 2; i++) {
    //     String tem = tap.substring(4 * i, 4 * (i + 1));
    //     // 二进制转十进制 Integer.valueOf("0101",2).toString()
    //     m = Integer.valueOf(tem, 2);
    //     // 十进制转成十六进制： Integer.toHexString(int i)
    //     n.append(Integer.toHexString(m));
    //   }
    //   System.out.println("后2位：" + n);
    // } else {
    //   System.out.println("tapButtonStatus 长度异常: " + tapButtonStatus.length());
    // }
    //
    // finalString = y + n.toString();
    //
    // System.out.println("buttonStatus: " + finalString);
    //
    // return finalString;


    String finalString = "";

    // 处理拨动开关按钮状态
    String s = switchButtonStatus;   //"10100010100000000001001000111111"   a280123f
    int x;
    // y -> 前8位，表示switchButtonStatus
    String y = "";
    if (s.length() > 32) {
      System.out.println("超长: " + s.length());
    } else if (s.length() == 32) {
      for (int i = 0; i < 8; i++) {
        String tem = s.substring(4 * i, 4 * (i + 1));
        // 二进制转十进制 Integer.valueOf("0101",2).toString()
        x = Integer.valueOf(tem, 2);
        // 十进制转成十六进制： Integer.toHexString(int i)
        y = y + Integer.toHexString(x).toString();
      }
      System.out.println("前8位：" + y);
    } else {
      System.out.println("switchButtonStatus 长度缺失: " + s.length());
    }

    /*
     * tapbutton处理
     *
     * 6位二进制转2位16进制
     * */
    //t 末尾补两个0 --> 10110000  176  b0      10011000  152  98     11101100  236  ec
    String t = tapButtonStatus;
    int m;
    String n = "";

    if (t.length() == 6) t = t + "00";

    if (t.length() == 8) {
      for (int i = 0; i < 2; i++) {
        String tem = t.substring(4 * i, 4 * (i + 1));
        // 二进制转十进制 Integer.valueOf("0101",2).toString()
        m = Integer.valueOf(tem, 2);
        // 十进制转成十六进制： Integer.toHexString(int i)
        n = n + Integer.toHexString(m).toString();
      }
      System.out.println("后2位：" + n);
    } else {
      System.out.println("tapButtonStatus 长度异常: " + t.length());
    }

    finalString = y + n;

    System.out.println("buttonStatus: " + finalString);

    return finalString;
  }
}
