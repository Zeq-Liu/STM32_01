package com.yunfd.web.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ZhouRecordVO implements Serializable {

    private int DayNum;
    private int ZhouNum;
    private List<Object> ZhouMessage;

    public ZhouRecordVO(){

    }


}
