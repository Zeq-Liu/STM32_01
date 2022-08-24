package com.yunfd.util.core;

import com.yunfd.config.CommonParams;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @Description
 * @Author LiuZequan
 * @Date 2022/5/10 16:52
 * @Version 1.0
 */
public class FileConversion {
    public static DatagramPacket FileConvertDatagrams(InetSocketAddress socketAddress, byte[] bytes, int count) {
        // length：文件片段长度
        int length = CommonParams.sliceSize;

        // 文件的最后一截长度
        if (bytes.length / CommonParams.sliceSize + 1 == count) {
            length = bytes.length % CommonParams.sliceSize;
        }

        byte[] fragment = new byte[length];
        System.arraycopy(bytes, (count - 1) * 12800, fragment, 0, length);

        return new DatagramPacket(Unpooled.copiedBuffer("File#" + (count) + "#" + new String(fragment), CharsetUtil.UTF_8), socketAddress);
    }
}
