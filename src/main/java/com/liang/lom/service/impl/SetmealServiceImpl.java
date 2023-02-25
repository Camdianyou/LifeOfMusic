package com.liang.lom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liang.lom.common.CustomException;
import com.liang.lom.common.R;
import com.liang.lom.dto.SetmealDto;
import com.liang.lom.entity.Setmeal;
import com.liang.lom.entity.SetmealDish;
import com.liang.lom.mapper.SetmealMapper;
import com.liang.lom.service.SetmealDishService;
import com.liang.lom.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Transactional
    public void addSetmeal(SetmealDto setmealDto) {
        // 保存菜品的基本信息
        this.save(setmealDto);
        Long setmealId = setmealDto.getId();

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        // 保存套餐中的每一个菜品
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void removeSetmeal(List<Long> ids) {
        // select count(*) from setmeal where id in ids and where status = 1;
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        // 正在售卖的不能删除
        queryWrapper.eq(Setmeal::getStatus, 1);
        // 已删除的不能早被删除
        queryWrapper.eq(Setmeal::getIsDeleted, 0);
        // 记录没有被删除，且没有停止售卖的套餐
        int count = setmealService.count(queryWrapper);
        if (count > 0) {
            // 不能删除
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        // 删除套餐,将isDeleted修改为0
        List<Setmeal> setmealList = new ArrayList<>();
        setmealList = setmealService.listByIds(ids).stream().map(item -> {
            item.setIsDeleted(1);
            return item;
        }).collect(Collectors.toList());

        // 首先删除套餐表中的数据
        setmealService.updateBatchById(setmealList);

        // 删除关系表中的数据setmeal_dish
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        List<SetmealDish> setmealDishes = new ArrayList<>();
        setmealDishes = setmealDishService.list(dishLambdaQueryWrapper).stream().map(item -> {
            item.setIsDeleted(1);
            return item;
        }).collect(Collectors.toList());

        // 删除套餐和菜品关系列表中的数据
        setmealDishService.updateBatchById(setmealDishes);
    }
}
