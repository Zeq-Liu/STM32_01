package com.yunfd;

import com.yunfd.domain.CbUseTime;
import com.yunfd.service.BoardOperationService;
import com.yunfd.service.CbUseTimeService;
import com.yunfd.util.RedisUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestWork {

  @Autowired
  public RedisUtils redisUtils;

  @Autowired
  public BoardOperationService boardOperationService;

  @Autowired
  public RedisTemplate redisTemplate;

  private final String connectionHashName = "connection";
  private final String userInfo = "test";

  @Test
  public void test01() {
    List<String> list = boardOperationService.readSteps(userInfo);
    System.out.println(list);
  }
}
