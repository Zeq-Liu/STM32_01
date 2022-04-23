package com.yunfd.web;

import com.baomidou.mybatisplus.service.IService;
import com.yunfd.domain.BaseModel;
import com.yunfd.web.vo.ResultVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 通用Controller（增删改查）
 * @Author yunfd
 */
public abstract class BaseController<S extends IService<T>, T extends BaseModel<T>> {

    @Autowired
    protected S service;

    /**
     * 新增
     * @param t
     * @return
     */
    @ApiOperation("新增")
    @PostMapping
    public ResultVO create(@RequestBody T t) {

        t.setCreateTime(new Date());
        t.setUpdateTime(new Date());
        if(service.insert(t)){
            return ResultVO.ok();
        }else{
            return ResultVO.error();
        }
    }

    /**
     * 更新
     * @param t
     * @return
     */
    @ApiOperation("更新")
    @PutMapping
    public ResultVO update(@RequestBody T t) {

        t.setUpdateTime(new Date());
        if(service.updateById(t)){
            return ResultVO.ok();
        }else{
            return ResultVO.error();
        }
    }

    /**
     * 根据id获取实体对象
     * @param id
     * @return
     */
    @ApiOperation("根据id获取实体对象")
    @GetMapping("/{id}")
    public T getInfo(@PathVariable String id) {
        return service.selectById(id);
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable String id) {
        if(service.deleteById(id)){
            return ResultVO.ok();
        }else{
            return ResultVO.error();
        }
    }
}
