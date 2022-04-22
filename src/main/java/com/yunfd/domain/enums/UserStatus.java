package com.yunfd.domain.enums;
/**
 * Created with IntelliJ IDEA.
 * User: xuanjiazhen
 * Date: 2018/2/24
 * Time: 下午4:16
 */
import java.io.Serializable;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
/**
 * @Author
 */
public enum UserStatus implements IEnum {
    able(1, "启用"),
    disable(0, "禁用");

    private int value;
    private String desc;

    UserStatus(final int value, final String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Serializable getValue() {
        return this.value;
    }
    @JsonValue
    public String getDesc(){
        return this.desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
