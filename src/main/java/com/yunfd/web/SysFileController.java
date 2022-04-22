package com.yunfd.web;

import cn.hutool.core.io.FileUtil;
import com.yunfd.config.CommonParams;
import com.yunfd.domain.SysFile;
import com.yunfd.service.BoardOperationService;
import com.yunfd.service.SysFileService;
import com.yunfd.web.vo.ResultVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Date;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/file")
public class SysFileController extends BaseController<SysFileService, SysFile> {

  @Autowired
  private SysFileService sysFileService;
  @Autowired
  private BoardOperationService boardOperationService;

  @ApiOperation(value = "通过文件类型获取文件列表", notes = "获取文件列表")
  @GetMapping("/getFileList")
  public ResultVO getFileList(@RequestParam("filetype") String filetype) {
    List<SysFile> sysFileList = sysFileService.selectByFileType(filetype);
    return ResultVO.ok(sysFileList);
  }

  // keypoint 只接受bit文件!!!
  @ApiOperation("上传bit文件")
  @PostMapping("/uploadBitFile")
  public ResultVO uploadBitFile(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws Exception {
    String originalFilename = file.getOriginalFilename();
    String bitReg = ".*?\\.bit$";

    if (originalFilename.matches(bitReg)) {
      try {
        //后缀成功匹配
        String token = request.getHeader("token");
        String filePath = CommonParams.getFullBitFilePath(token);
        FileUtil.del(filePath);
        boardOperationService.clearSteps(token);

        saveFile(file, CommonParams.getBitFileBase(), filePath);

        SysFile sysFile = new SysFile();
        sysFile.setFileType("bit");
        sysFile.setCreateTime(new Date());
        sysFile.setDirectfilePath(filePath);
        sysFile.setCreateUserId(token);
        sysFileService.insertOrUpdate(sysFile);
        boardOperationService.recordBitFileToBoardForTheFirstTime(token, filePath);
        return ResultVO.ok("成功");
      } catch (Exception e) {
        e.printStackTrace();
        return ResultVO.error("出错啦");
      }
    } else return ResultVO.error("文件格式不对");
  }

  //解冻时恢复bit
  @ApiOperation("解冻时恢复bit")
  @PostMapping("/reloadBitFile")
  public ResultVO reloadBitFile(HttpServletRequest request) throws Exception{
    String token = request.getHeader("token");
    String filePath = CommonParams.getFullBitFilePath(token);
    if(FileUtil.exist(filePath)){
      boardOperationService.recordBitFileToBoardForTheFirstTime(token, filePath);
      return ResultVO.ok("成功");
    }
    else return ResultVO.error("找不到服务器上的bit文件");
  }

  //保存文件
  public static boolean saveFile(MultipartFile multipartFile, String basePath, String fullPath) throws IOException {
    try {
      if (multipartFile.isEmpty())return false;
      File file = new File(fullPath);
      if(!file.getParentFile().exists()){
        file.getParentFile().mkdirs();
      }
      multipartFile.transferTo(file);
    } catch (Exception e) {
      System.out.println("error occurs while saving a file");
      return false;
    }
    return true;
  }

  //保存文件
//  public static String saveFile(MultipartFile multipartFile, String path, String fileName) throws IOException {
//    File file = new File(path);
//    if (!file.exists()) {
//      file.mkdirs();
//    }
//    FileInputStream fileInputStream = (FileInputStream) multipartFile.getInputStream();
//    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path + File.separator + fileName));
//    byte[] bs = new byte[1024];
//    int len;
//    while ((len = fileInputStream.read(bs)) != -1) {
//      bos.write(bs, 0, len);
//    }
//    bos.flush();
//    bos.close();
//    System.out.println("saveFile: File saved! path: " + path + File.separator + fileName);
//    return fileName;
//  }
}




