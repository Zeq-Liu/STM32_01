package com.yunfd.web.vo;


import java.io.Serializable;

/**
 * @Author yunfd
 */
public class SmartSort implements Serializable {

    private String predicate;

    private boolean reverse;

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public boolean getReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
}
