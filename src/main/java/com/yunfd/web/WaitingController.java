package com.yunfd.web;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.CircuitBoard;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.domain.vo.UserVo;
import com.yunfd.service.CircuitBoardService;
import com.yunfd.service.WaitingService;
import com.yunfd.util.RedisUtils;
import com.yunfd.web.vo.ResultVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/waiting")
public class WaitingController {
  @Autowired
  private WaitingService waitingService;
  @Autowired
  private CircuitBoardService circuitBoardService;
  @Autowired
  private RedisUtils redisUtils;

  //太久没操作之后要操作得先 keypoint 点击按钮入队
  @PostMapping("/intoLine")
  @ApiOperation("用户入队")
  public ResultVO waitInLine(HttpServletRequest request) {
    String token = request.getHeader("token");
    Long number = waitingService.getNumberOfPeopleInFrontOfUser(token);
    if (Validator.isNotNull(number)) return ResultVO.error("您已在队列中");
    waitingService.waitInLine(token);
    return ResultVO.ok("进入队列");
  }

  @GetMapping("/getBefore")
  @ApiOperation("获取该用户前面的人数")
  public ResultVO getBefore(HttpServletRequest request) {
    String token = request.getHeader("token");
    return ResultVO.ok(waitingService.getNumberOfPeopleInFrontOfUser(token));
  }

  @PostMapping("/checkAvailability")
  @ApiOperation("用户监听板卡分配事件")
  public ResultVO checkAvailability(HttpServletRequest request) {
    String token = request.getHeader("token");
    Long number = waitingService.getNumberOfPeopleInFrontOfUser(token);
    if (Validator.isNotNull(number) && number == 0) {
      CircuitBoard board = circuitBoardService.getAFreeBoard();
      if (Validator.isNotNull(board)) {
        waitingService.unfreezeConnection(token, board);
        Object o = redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token);
        return ResultVO.ok(Convert.convert(UserConnectionVo.class, o));
      }
      return ResultVO.ok("您前面还有: " + number + "人排队");
    }
    return ResultVO.error("出错了");
  }

  //用户时长的维护
  @ApiOperation("获取用户的倒计时记录")
  @GetMapping("/getTimeObj")
  public ResultVO getTimeObj(HttpServletRequest request) {
    try {
      String token = request.getHeader("token");
      UserConnectionVo vo = waitingService.getConnectionObj(token);
      if (Validator.isNull(vo)) return ResultVO.error();
      vo.setLeftSeconds(redisUtils.getExpire(CommonParams.REDIS_CONN_SHADOW_PREFIX + token));
      return ResultVO.ok(vo);
    } catch (Exception e) {
      return ResultVO.error();
    }
  }
}
