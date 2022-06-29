package com.yunfd.service;

import com.yunfd.domain.CircuitBoard;
import com.yunfd.domain.vo.UserConnectionVo;

import javax.servlet.http.HttpServletRequest;

public interface WaitingService {
    //排队队列的维护
    Long getNumberOfPeopleInFrontOfUser(String userToken);

    boolean waitInLine(String userToken);

    Long outOfLine(String userToken);

    Long getTotalWaitingNum();

    //用户时长的维护
    UserConnectionVo getConnectionObj(String userToken);

    UserConnectionVo createConnectionObj(HttpServletRequest request, String userToken);

    boolean freezeConnection(String userToken);

    //有空闲电路板并且已经排到
    Long unfreezeConnection(String userToken, CircuitBoard board);
}

