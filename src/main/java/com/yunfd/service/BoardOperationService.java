package com.yunfd.service;

import java.util.List;

/**
 * 用户操作
 */
public interface BoardOperationService {
    //初始化用户的操作步骤“文件”
    boolean initOperationList(String userInfo);

    //追加步骤到文件中
    boolean appendStepsToList(String userInfo, List<String> steps);

    //追加单个步骤到文件中
    boolean appendAStepToList(String userInfo, String step);

    //读取所有的步骤到list
    List<String> readSteps(String userInfo);

    //清除用户的操作历史
    void clearSteps(String userInfo);

    //还原场景到新板卡
    boolean reloadEnv(String userInfo, String longId);

    //往板卡中第一次烧录bin文件片段
    void recordBinFileToBoardForTheFirstTime(String token, String filePath);
}
