package com.yunfd.util;

/**
 * 生成时间戳加随机数的  唯一id
 */
public class IdUtils {

        private static byte[] lock = new byte[0];

        // 位数，默认是8位
        private final static long w = 100000000;

        public static String createID() {
            long r = 0;
            synchronized (lock) {
                r = (long) ((Math.random() + 1) * w);
            }

            return System.currentTimeMillis() + String.valueOf(r).substring(1);
        }
    }

