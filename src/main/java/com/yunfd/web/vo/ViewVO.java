package com.yunfd.web.vo;/**
 * Created with IntelliJ IDEA.
 * User: xuanjiazhen
 * Date: 2018/5/11
 * Time: 下午6:50
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ViewVO {
    private static final long serialVersionUID = 1L;
    private String date;
    private int num;
    public ViewVO(){

    }
    public ViewVO(String date,int num){
        this.date = date;
        this.num = num;
    }
}
