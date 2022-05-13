package com.yunfd.util.core;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @Description
 * @Author LiuZequan
 * @Date 2022/5/10 16:48
 * @Version 1.0
 */

public class FileUploadUtil implements Serializable {
    private static final long serialVersionUID = 1L;
    private File file;// 文件
    private String file_md5;// 文件名
    private int starPos;// 开始位置
    private byte[] bytes;// 开始位置
    private int endPos;// 结尾位置
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public String getFile_md5() {
        return file_md5;
    }
    public void setFile_md5(String file_md5) {
        this.file_md5 = file_md5;
    }
    public int getStarPos() {
        return starPos;
    }
    public void setStarPos(int starPos) {
        this.starPos = starPos;
    }
    public byte[] getBytes() {
        return bytes;
    }
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    public int getEndPos() {
        return endPos;
    }
    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "FileUploadFile{" +
                "file=" + file +
                ", file_md5='" + file_md5 + '\'' +
                ", starPos=" + starPos +
                ", bytes=" + Arrays.toString(bytes) +
                ", endPos=" + endPos +
                '}';
    }
}

