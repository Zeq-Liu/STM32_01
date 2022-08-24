package com.yunfd.util.core;

import cn.hutool.core.io.file.FileNameUtil;
import com.yunfd.STM32JavaApplication;
import com.yunfd.config.CommonParams;
import com.yunfd.util.ByteToFileUtil;
import com.yunfd.util.RedisUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static com.yunfd.util.ByteToFileUtil.bytesToHexString;

/**
 * @Description
 * @Author LiuZequan
 * @Date 2022/4/20 11:32
 * @Version 2.0
 */

@Slf4j
public class SendMessageToCB {

    // public static DatagramPacket datagramPacketMsg;
    public static String backtrack;

    /**
     * 烧录过程  在bit文件传到位后进行调用
     */

    public static void recordBinOnCB(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String BIT_FILE_PATH, int count) {
        //count为发送文件时的第几次发送标志，从0开始，0和1有其他意思

        //keypoint 更新操作计时器
        String token = FileNameUtil.mainName(BIT_FILE_PATH);
        //获取redis操作工具
        RedisUtils redisUtils = STM32JavaApplication.ac.getBean(RedisUtils.class);
        redisUtils.set(CommonParams.REDIS_OP_TTL_PREFIX + token, true, CommonParams.REDIS_OP_TTL_LIMIT);

        byte[] bytes = ByteToFileUtil.getBytes(BIT_FILE_PATH);
        double limit = Math.ceil(bytes.length / (CommonParams.sliceSize + 0.0));
        System.out.println("limit: " + (int) limit);
        String md5 = MD5Util.generateMD5(BIT_FILE_PATH);

        System.out.println("count：" + count);
        //发送到板卡 第0次和第1次数据发送有其他意思
        if (count == 1) {
            System.out.println("文件传输次数：" + limit);
            sendMsgByUdp(ctx, socketAddress, "Begin#" + String.format("%02d", (int) limit));
            // } else if (count == 1) {
            //     System.out.println("文件的MD5码：" + md5);
            //     sendMsgByUdp(ctx, socketAddress, "MD5#" + md5);
        } else if (count - 2 < limit) {
            // length：文件片段长度
            int length = CommonParams.sliceSize;

            // 文件的最后一截长度
            if (bytes.length / CommonParams.sliceSize == count - 2) {
                length = bytes.length % CommonParams.sliceSize;
            }
            //文件段存储变量
            byte[] fragment = new byte[length];
            System.arraycopy(bytes, (count - 2) * CommonParams.sliceSize, fragment, 0, length);
            // System.out.println("第 " + (count - 2) + " 次发送文件片段 ");
            //bytesToHexString(("File#" + String.format("%02d",(count - 1)) + "#").getBytes())

            System.out.println(bytesToHexString(("File#" + String.format("%02d", (count - 1)) + "#").getBytes()) + bytesToHexString(fragment));
            sendMsgByUdp2(ctx, socketAddress, bytesToHexString(("File#" + String.format("%02d", (count - 1)) + "#").getBytes()) + bytesToHexString(fragment));
        } else {
            System.out.println("烧录完毕");
        }
    }

    /**
     * 发送重置消息到板卡
     * @param ctx
     * @param socketAddress
     */
    public static void sendInitToCB(ChannelHandlerContext ctx, InetSocketAddress socketAddress) {
        sendMsgByUdp(ctx, socketAddress, "Init#");
        System.out.println("发送重置消息成功！");
    }

    /**
     * @param ctx
     * @param BUTTON_STRING
     */
    public static void sendButtonStringToCB(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String BUTTON_STRING) {
        System.out.println("KB#" + BUTTON_STRING);
        sendMsgByUdp(ctx, socketAddress, "KB#" + BUTTON_STRING );
    }

    /**
     * @param ctx {ChannelHandlerContext}
     */
    public static void sendENDToCB(ChannelHandlerContext ctx, InetSocketAddress socketAddress) {
        // 释放板子
        sendMsgByUdp(ctx, socketAddress, "NNN");
    }


    /**
     * 处理按钮字符串 16位二进制转4位16进制
     * @param buttonStatus {String}
     * @return
     */
    public static String ProcessButtonString(String buttonStatus){
        String finalString = "";

        // 处理按钮状态 16位二进制转4位16进制
        int x;
        // y -> 前8位，表示switchButtonStatus
        StringBuilder y = new StringBuilder();
        if (buttonStatus.length() == 16) {
            for (int i = 0; i < 16; i++) {
                String tem = buttonStatus.substring(4 * i, 4 * (i + 1));
                // 二进制转十进制 Integer.valueOf("0101",2).toString()
                x = Integer.valueOf(tem, 2);
                // 十进制转成十六进制： Integer.toHexString(int i)
                y.append(Integer.toHexString(x));
            }
            System.out.println("处理按钮状态 16位二进制转4位16进制：" + y);
        } else {
            System.out.println("buttonStatus 长度异常: " + buttonStatus.length());
        }

        finalString += y;

        return finalString;
    }

    // public static String ProcessButtonString(String switchButtonStatus, String tapButtonStatus) {
    //
    //
    //     // String finalString = "";
    //     //
    //     // // 处理拨动开关按钮状态
    //     // int x;
    //     // // y -> 前8位，表示switchButtonStatus
    //     // StringBuilder y = new StringBuilder();
    //     // if (switchButtonStatus.length() > 32) {
    //     //   System.out.println("超长: " + switchButtonStatus.length());
    //     // } else if (switchButtonStatus.length() == 32) {
    //     //   for (int i = 0; i < 8; i++) {
    //     //     String tem = switchButtonStatus.substring(4 * i, 4 * (i + 1));
    //     //     // 二进制转十进制 Integer.valueOf("0101",2).toString()
    //     //     x = Integer.valueOf(tem, 2);
    //     //     // 十进制转成十六进制： Integer.toHexString(int i)
    //     //     y.append(Integer.toHexString(x));
    //     //   }
    //     //   System.out.println("前8位：" + y);
    //     // } else {
    //     //   System.out.println("switchButtonStatus 长度缺失: " + switchButtonStatus.length());
    //     // }
    //     //
    //     // /*
    //     //  * tapbutton处理
    //     //  *
    //     //  * 6位二进制转2位16进制
    //     //  * */
    //     // //t 末尾补两个0 --> 10110000  176  b0      10011000  152  98     11101100  236  ec
    //     // int m;
    //     // StringBuilder n = new StringBuilder();
    //     // if (tapButtonStatus.length() == 6) {
    //     //   String tap = tapButtonStatus + "00";
    //     //   for (int i = 0; i < 2; i++) {
    //     //     String tem = tap.substring(4 * i, 4 * (i + 1));
    //     //     // 二进制转十进制 Integer.valueOf("0101",2).toString()
    //     //     m = Integer.valueOf(tem, 2);
    //     //     // 十进制转成十六进制： Integer.toHexString(int i)
    //     //     n.append(Integer.toHexString(m));
    //     //   }
    //     //   System.out.println("后2位：" + n);
    //     // } else {
    //     //   System.out.println("tapButtonStatus 长度异常: " + tapButtonStatus.length());
    //     // }
    //     //
    //     // finalString = y + n.toString();
    //     //
    //     // System.out.println("buttonStatus: " + finalString);
    //     //
    //     // return finalString;
    //
    //
    //     String finalString = "";
    //
    //     // 处理拨动开关按钮状态
    //     String s = switchButtonStatus;   //"10100010100000000001001000111111"   a280123f
    //     int x;
    //     // y -> 前8位，表示switchButtonStatus
    //     String y = "";
    //     if (s.length() > 32) {
    //         System.out.println("超长: " + s.length());
    //     } else if (s.length() == 32) {
    //         for (int i = 0; i < 8; i++) {
    //             String tem = s.substring(4 * i, 4 * (i + 1));
    //             // 二进制转十进制 Integer.valueOf("0101",2).toString()
    //             x = Integer.valueOf(tem, 2);
    //             // 十进制转成十六进制： Integer.toHexString(int i)
    //             y = y + Integer.toHexString(x).toString();
    //         }
    //         System.out.println("前8位：" + y);
    //     } else {
    //         System.out.println("switchButtonStatus 长度缺失: " + s.length());
    //     }
    //
    //     /*
    //      * tapbutton处理
    //      *
    //      * 6位二进制转2位16进制
    //      * */
    //     //t 末尾补两个0 --> 10110000  176  b0      10011000  152  98     11101100  236  ec
    //     String t = tapButtonStatus;
    //     int m;
    //     String n = "";
    //
    //     if (t.length() == 6) t = t + "00";
    //
    //     if (t.length() == 8) {
    //         for (int i = 0; i < 2; i++) {
    //             String tem = t.substring(4 * i, 4 * (i + 1));
    //             // 二进制转十进制 Integer.valueOf("0101",2).toString()
    //             m = Integer.valueOf(tem, 2);
    //             // 十进制转成十六进制： Integer.toHexString(int i)
    //             n = n + Integer.toHexString(m).toString();
    //         }
    //         System.out.println("后2位：" + n);
    //     } else {
    //         System.out.println("tapButtonStatus 长度异常: " + t.length());
    //     }
    //
    //     finalString = y + n;
    //
    //     System.out.println("buttonStatus: " + finalString);
    //
    //     return finalString;
    // }


    // udp 发送消息
    public static void sendMsgByUdp(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String msg) {
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8), socketAddress));
    }

    //  将 16 进制的字符串转成字符数组
    public static byte[] getHexBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    public static void sendMsgByUdp2(ChannelHandlerContext ctx, InetSocketAddress socketAddress, String msg) {
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(getHexBytes(msg)), socketAddress));
    }
}
