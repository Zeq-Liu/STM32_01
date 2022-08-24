package com.yunfd.web;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.CircuitBoard;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.service.CbUseTimeService;
import com.yunfd.service.CircuitBoardService;
import com.yunfd.service.WaitingService;
import com.yunfd.util.RedisUtils;
import com.yunfd.web.vo.ResultVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description
 * @Date 2022/6/1 1:47
 * @Version 2.0
 */

@RestController
@RequestMapping("/waiting")
public class WaitingController {
    @Autowired
    private WaitingService waitingService;
    @Autowired
    private CircuitBoardService circuitBoardService;
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CbUseTimeService cbUseTimeService;


    // @PostMapping("/intoLine")
    // @ApiOperation("用户入队，用户拥有token，但不一定需要板卡，这一步是进入队列，等待板卡的获取")
    // public ResultVO waitInLine(HttpServletRequest request) {
    //     String token = request.getHeader("token");
    //     Long number = waitingService.getNumberOfPeopleInFrontOfUser(token);
    //     if (Validator.isNotNull(number)) return ResultVO.error("您已在队列中");
    //     waitingService.waitInLine(token);
    //     return ResultVO.ok("进入队列");
    // }

    @GetMapping("/getBefore")
    @ApiOperation("获取该用户前面的人数")
    public ResultVO getBefore(HttpServletRequest request) {
        String token = request.getHeader("token");
        return ResultVO.ok(waitingService.getNumberOfPeopleInFrontOfUser(token));
    }

    @PostMapping("/checkAvailability")
    @ApiOperation("分配给用户一块空闲的板卡")
    public ResultVO checkAvailability(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long number = waitingService.getNumberOfPeopleInFrontOfUser(token);
        if (Validator.isNotNull(number) && number == 0) {
            CircuitBoard board = circuitBoardService.getAFreeBoard();
            if (Validator.isNotNull(board)) {
                // 先解冻 后面才能继续烧录
                waitingService.unfreezeConnection(token, board);


                // 获取对象
                Object o = redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token);

                //保存进 用户数据库
                UserConnectionVo connectionVo = Convert.convert(UserConnectionVo.class, o);
                boolean b = cbUseTimeService.saveUserUsingData(connectionVo);
                if (b) {
                    System.out.println("存档成功！");
                }
                return ResultVO.ok(Convert.convert(UserConnectionVo.class, o));
            }else {
                return ResultVO.error("当前没有空闲板卡！");
            }
        }
        return ResultVO.error();
    }

    //用户时长的维护
    @ApiOperation("获取当前用户的剩余使用时间 对象中的leftSeconds为剩余秒数")
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
