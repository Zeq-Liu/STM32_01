package com.yunfd.web;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.domain.vo.UserVo;
import com.yunfd.service.IdentifyService;
import com.yunfd.service.WaitingService;
import com.yunfd.util.RedisUtils;
import com.yunfd.web.vo.ResultVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Description
 * @Date 2022/6/1 10:25
 * @Version 3.0
 */

@RestController
@RequestMapping("/identify")
public class IdentifyController {
    @Resource
    private RedisUtils redisUtils;

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private IdentifyService identifyService;

    @PostMapping("/generateSaltAndReturnToken")
    @ApiOperation("获取服务端授权的token") //颁布token，生成用户信息，并将用户信息存储在redis中
    public ResultVO generateSaltAndReturnToken(HttpServletRequest request, @RequestBody UserVo userVo) {
        boolean b = CommonParams.isLegalParamsWithoutSalt(userVo);
        if (b) {
            String salt = IdUtil.simpleUUID();
            userVo.setSalt(salt);
            String token = CommonParams.generateUserToken(userVo);
            if (Validator.isNull(token)) return ResultVO.error("身份信息有误");
            // 无误则 将身份信息限时存储在redis中
            boolean b_ttl = redisUtils.set(CommonParams.REDIS_TTL_PREFIX + token, true, CommonParams.REDIS_TTL_LIMIT);

            // 生成 billid流水号 记录用户使用情况
            String billId = IdUtil.simpleUUID();
            //生成shadow和时间obj，保存到redis
            boolean b_shadow = redisUtils.set(CommonParams.REDIS_CONN_SHADOW_PREFIX + token, true);
            //生成时间对象waiting service
            UserConnectionVo userConnectionVo = waitingService.createConnectionObj(request, token);
            userConnectionVo.setBillId(billId);
            userConnectionVo.setUserIp(request.getRemoteAddr());
            boolean b_waiting = redisUtils.set(CommonParams.REDIS_CONN_PREFIX + token, userConnectionVo);
            waitingService.waitInLine(token);
            return b_ttl && b_shadow && b_waiting ? ResultVO.ok(token) : ResultVO.error("用户验证失败");
        }
        return ResultVO.error("身份信息有误");
    }

    @GetMapping("/fresh")
    @ApiOperation("刷新后端连接")
    public ResultVO fresh(HttpServletRequest request) {
        String token = request.getHeader("token");
        boolean b = redisUtils.set(CommonParams.REDIS_TTL_PREFIX + token, true, CommonParams.REDIS_TTL_LIMIT);
        return b ? ResultVO.ok("刷新成功") : ResultVO.error();
    }

    @PostMapping("/reload")
    @ApiOperation("掉线重连，分配板卡之后才使用")
    public ResultVO reload(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (Validator.isNotNull(token)) {
            Object o = redisUtils.get(CommonParams.REDIS_TTL_PREFIX + token);
            Object timeObj = redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token);
            if (Validator.isNotNull(o) && Validator.isNotNull(timeObj)) {

                UserConnectionVo vo = Convert.convert(UserConnectionVo.class, timeObj);
                if (Validator.isNotNull(vo) && Validator.isNotNull(vo.getLongId())) {
                    //返回板卡信息
                    UserConnectionVo connVo = Convert.convert(UserConnectionVo.class, redisUtils.get(CommonParams.REDIS_CONN_PREFIX + token));
                    long expire = redisUtils.getExpire(CommonParams.REDIS_CONN_SHADOW_PREFIX + token);
                    connVo.setLeftSeconds(expire);
                    return ResultVO.ok(connVo);
                } else {
                    Long number = waitingService.getNumberOfPeopleInFrontOfUser(token);
                    if (Validator.isNotNull(number) && number >= 0) {
                        return ResultVO.error("当前没有空闲板卡！");
                    } else return ResultVO.error();
                }
            }
        }
        return ResultVO.error("验证无效或身份已过期");
    }

    // @PostMapping("/generateBillId")
    // @ApiOperation("新用户进入平台分配流水号，用户端和redis都保存一份")
    // public ResultVO generateBillId(HttpServletRequest request) {
    //   String token = request.getHeader("token");
    //   if (Validator.isNull(waitingService.getNumberOfPeopleInFrontOfUser(token))) {
    //     String billId = IdUtil.simpleUUID();
    //     //生成shadow和时间obj，保存到redis
    //     boolean b_shadow = redisUtils.set(CommonParams.REDIS_CONN_SHADOW_PREFIX + token, true);
    //     //生成时间对象waiting service
    //     UserConnectionVo userConnectionVo = waitingService.createConnectionObj(request, token);
    //     userConnectionVo.setBillId(billId);
    //     boolean b_waiting = redisUtils.set(CommonParams.REDIS_CONN_PREFIX + token, userConnectionVo);
    //     waitingService.waitInLine(token);
    //     return b_shadow && b_waiting ? ResultVO.ok(billId) : ResultVO.error("用户验证失败");
    //   } else {
    //     return ResultVO.error("已生成过billId!");
    //   }
    // }

    @PostMapping("/isUserValid")
    @ApiOperation("判断用户的信息是否有效，防止信息伪造")
    // 不一定有用，或许后期有用 也说不定
    public ResultVO isUserValid(HttpServletRequest request) {
        String token = request.getHeader("token");
        return ResultVO.ok(identifyService.checkValidity(token));
    }
}
