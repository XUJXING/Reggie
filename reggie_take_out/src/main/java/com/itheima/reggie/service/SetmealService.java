package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.pojo.Setmeal;
import com.itheima.reggie.pojo.SetmealDto;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     *
     *新增套餐，同时要保持与菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);


    /**
     * 批量删除套餐，并且删除套餐与菜品关系表
     * @param ids
     */
    public void removeWithDish(List<Long> ids);


    /**
     * 根据id查询套餐信息并回显
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id);


    /**
     * 修改套餐
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);
}
