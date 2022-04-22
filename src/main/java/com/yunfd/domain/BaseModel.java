//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yunfd.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 通用实体（通用字段）
 * @Author yunfd
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseModel<T extends BaseModel> implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;

    private String createUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String delFlag;

    protected Serializable pkVal() {
        // TODO Auto-generated method stub
        return id;
    }
}
