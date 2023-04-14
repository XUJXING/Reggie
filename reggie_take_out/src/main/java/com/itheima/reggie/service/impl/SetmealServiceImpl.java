package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.pojo.Setmeal;
import com.itheima.reggie.pojo.SetmealDish;
import com.itheima.reggie.pojo.SetmealDto;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐，同时要保持与菜品的关联关系
     *
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> list = setmealDto.getSetmealDishes();

        list = list.stream().map((item) -> {
            //套餐菜品关系表没有套餐id，但是新增套餐后springboot会生成id，把新增后的套餐的id获取出来设置到表中
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(list);
    }


    /**
     * （批量）删除套餐，并且删除套餐与菜品关系表
     *
     * @param ids
     */
    @Transactional //操纵了两张表的数据，为了保持数据一致性，添加一个事务注解
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，确定是否删除（在售不可删除）
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);

        if (count > 0) {
            throw new CustomException("商品正在售卖中，不能删除");
        }

        //如果可以删除，那就先删除套餐表中的数据
        this.removeByIds(ids);

        //然后再删除套餐与菜品关系表中的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }


    /**
     * 根据id查询套餐信息并回显
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {

        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setmeal, setmealDto);

        //查询套餐菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getId, setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }


    /**
     * 修改套餐
     *
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
         //更新setmeal基本信息
        this.updateById(setmealDto);

        //更新setmeal_deish表信息delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //更新setmeal_dish表信息insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }
}
