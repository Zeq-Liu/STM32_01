package com.yunfd.domain;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.*;

/**
 * 数据库类--板卡
 * @Description
 * @Author LiuZequan
 * @Date 2022/6/17 11:45
 * @Version 2.0
 */


@TableName("cb")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CircuitBoard extends BaseModel<CircuitBoard> {

    // 有用
    //ip地址+端口
    private String cbIpPort;                    //32
    //端口
    // private int cbPort;
    //是否被占用
    private String status;
    //是否被预定 暂时无用
    private String isReserved;
    //是否被烧录
    private String isRecorded;

    // 存储板子编号
    private String longId;                  //50

    // 无用

    // private String lightStatus;             //32
    // private String switchButtonStatus;      //todo 准备删除
    // private String tapButtonStatus;         //todo 准备删除
    // private String isVideo;                 //todo 准备删除

    public CircuitBoard(String cbIpPort) {
        this.cbIpPort = cbIpPort;
    }
}
