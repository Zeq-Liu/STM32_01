package com.yunfd.web.vo;

import java.io.Serializable;

/**
 * 状态码设置：
 * code: 0 1 2
 * 0：成功
 * 1：已知错误
 * 2：未知错误
 */

public class ResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final Integer CODE = 0;

    private Integer code;

    private String msg;

    private Object result;

    public ResultVO() {

    }

    public ResultVO(Integer code) {
        this.code = code;
    }

    public ResultVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultVO(Integer code, Object result) {
        this.code = code;
        this.result = result;
    }

    public ResultVO(Integer code, String msg, Object result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    public ResultVO(Object result) {
        this.result = result;
    }

    public static ResultVO error() {
        return error(2, "未知异常，请联系管理员");
    }

    public static ResultVO error(String msg) {
        return error(1, msg);
    }

    public static ResultVO error(Integer code, String msg) {
        return new ResultVO(code, msg);
    }

    public static ResultVO ok(String msg) {
        return new ResultVO(CODE, msg);
    }

    public static ResultVO ok(Object result) {
        return new ResultVO(CODE, result);
    }

    public static ResultVO ok() {
        return new ResultVO(CODE);
    }

    public static ResultVO ok(Integer code, String msg, Object result) {
        return new ResultVO(code, msg, result);
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        // Create a copy, don't share the array
        return "code:" + this.getCode() + "msg:" + this.getMsg() + "result:" + this.getResult();
    }
}
