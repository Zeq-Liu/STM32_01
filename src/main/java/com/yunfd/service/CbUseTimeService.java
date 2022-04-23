package com.yunfd.service;

import com.baomidou.mybatisplus.service.IService;
import com.yunfd.domain.CbUseTime;
import com.yunfd.domain.vo.UserConnectionVo;

public interface CbUseTimeService extends IService<CbUseTime> {

  boolean saveUserUsingData(UserConnectionVo connectionVo);
}
