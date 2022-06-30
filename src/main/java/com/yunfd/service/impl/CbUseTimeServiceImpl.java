package com.yunfd.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.CbUseTime;
import com.yunfd.domain.vo.UserConnectionVo;
import com.yunfd.mapper.CbUseTimeMapper;
import com.yunfd.service.CbUseTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class CbUseTimeServiceImpl extends ServiceImpl<CbUseTimeMapper, CbUseTime> implements CbUseTimeService {

    @Autowired
    private CbUseTimeService cbUseTimeService;

    /**
     * 根据 redis里面的连接信息，保存用户的使用情况
     *
     * @param connectionVo
     */
    @Override
    public boolean saveUserUsingData(UserConnectionVo connectionVo) {
        // todo 提前存档？
        CbUseTime time = new CbUseTime();
        String token = connectionVo.getToken();
        String[] info = token.split("_");
        time.setCbIpPort(connectionVo.getCbIpPort());
        time.setCbLongId(connectionVo.getLongId());
        time.setPlatformTag(info[0]);
        time.setSchoolName(info[1]);
        time.setWorkId(info[2]);
        // time.setUsername();
        time.setUserIp(connectionVo.getUserIp());
        time.setCreateTime(connectionVo.getCreateTime());
        time.setId(connectionVo.getBillId());
        time.setFileUploadNum(1);
        time.setDuration((int) ((CommonParams.REDIS_CONN_SHADOW_LIMIT - connectionVo.getLeftSeconds() + (new Date().getTime() - connectionVo.getUpdateTime().getTime()) / 1000) / 60));
        return cbUseTimeService.insertOrUpdate(time);
    }
}