package com.yunfd.config;

import cn.hutool.core.lang.Validator;
import com.yunfd.HduFPGAJavaApplication;
import com.yunfd.util.RedisUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
    //所有请求要带token，token由后端提供
    String token = httpServletRequest.getHeader("token");
    String reg = "^(.+_){2}.+$";
    if (Validator.isNull(token) || token.equals("") || (!token.matches(reg))) return false;
    else {
      RedisUtils redisUtils = HduFPGAJavaApplication.ac.getBean(RedisUtils.class);
      Object validity = redisUtils.get(CommonParams.REDIS_TTL_PREFIX + token);
      return !Validator.isNull(validity);
    }
  }

  @Override
  public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

  }

  @Override
  public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

  }
}
