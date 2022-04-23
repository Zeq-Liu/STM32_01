package com.yunfd.service;

import com.baomidou.mybatisplus.service.IService;
import com.yunfd.domain.SysFile;


import java.util.List;

public interface SysFileService extends IService<SysFile> {
    /**
     * 根据文件名查询文件
     * @param fileName
     * @return
     */
    SysFile findByFileName(String fileName);

    /**
     * 查询类型文件列表
     * @param fileType 文件类型
     * @return 查询该文件类型的文件列表
     */
    List<SysFile> selectByFileType(String fileType);


}
