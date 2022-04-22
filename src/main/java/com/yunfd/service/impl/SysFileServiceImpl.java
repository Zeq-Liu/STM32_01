package com.yunfd.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.yunfd.domain.SysFile;
import com.yunfd.mapper.SysFileMapper;
import com.yunfd.service.SysFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysFileServiceImpl  extends ServiceImpl<SysFileMapper,SysFile> implements SysFileService {

    @Autowired
    private SysFileMapper sysFileMapper;

    @Override
    public SysFile findByFileName(String fileName) {
        List<SysFile> list = selectList(new EntityWrapper<>(new SysFile(fileName)));
        if (list!=null&&list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<SysFile> selectByFileType(String fileType) {
//        NettySocketHolder a = NettySocketHolder.getMAP();
        List<SysFile> list =  selectList(new EntityWrapper<SysFile>().eq("file_type",fileType));
        return list;
    }




}
