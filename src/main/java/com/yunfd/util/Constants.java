package com.yunfd.util;

/**
 * @Author yunfd
 */
public final class Constants {

    public static final String SPRING_PROFILE_DEVELOPMENT = "dev";
    public static final String UNDERLINE = "_";
    public static final String ACCOUNT_DISABLE = "0";

    //地图地点相关状态

    //未购买
    public static final String POSITION_ZERO="0";
    //买了没开课
    public static final String POSITION_ONE="1";
    //买了 已开课 已解锁
    public static final String POSITION_TWO="2";
    //买了 已开课 未解锁
    public static final String POSITION_THREE="3";

    //是否开课
    public static final String OPEN="0";
    public static final String CLOSE="1";

    //题目类型
    public static final String FORCOURSE="0";
    public static final String FORREPORT="1";


    //用户解锁状态，分享朋友圈之后解锁为1，未解锁为0
    public static final String DISAVIABLE="0";
    public static final String AVIABLE="1";
//默认密码，避免用户登录
    public static final String DEFAULT_PASSWORD =  "mayuanxiyouceshidemorenmima.lejcyit";
    public static final String DEFAULT_PASSWORD_MAYUAN =  "mayuan";
    //体验课Id
    public static final String COURSEID="e3d16048b3ac4172bf46a23b56d3b530";

    //渠道 channel
    public static final String WXPAY="1";
    public static final String ALIPAY="0";
    //支付状态 channel
    public static final String PAY_WAIT="0";
    public static final String PAY_CANCEL="1";
    public static final String PAY_SUCESS="2";
    public static final String PAY_FAIL="3";


}
