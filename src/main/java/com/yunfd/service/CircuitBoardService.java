package com.yunfd.service;

import com.baomidou.mybatisplus.service.IService;
import com.yunfd.domain.CircuitBoard;

public interface CircuitBoardService extends IService<CircuitBoard> {

  CircuitBoard findByCBIP(String ip);

  CircuitBoard findByCBID(String longId);

  CircuitBoard getAFreeBoard();

  boolean freeCB(String token);

  String simplyFreeCB(String cbIp);

}