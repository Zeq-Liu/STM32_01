package com.yunfd.service;

import com.baomidou.mybatisplus.service.IService;
import com.yunfd.domain.CircuitBoard;

/**
 * Created by DJ on 2018/7/8.
 */
public interface CircuitBoardService extends IService<CircuitBoard> {

  CircuitBoard findByCBIP(String ip);

  CircuitBoard findByCBID(String longId);

  CircuitBoard getAFreeBoard();

  boolean freeCB(String token);

  String simplyFreeCB(String cbIp);

}