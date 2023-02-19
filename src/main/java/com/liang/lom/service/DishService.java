package com.liang.lom.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liang.lom.dto.DishDto;
import com.liang.lom.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入菜品对应的口味数据,需要操作两张表dish,dish_flavor
    public void addDishAndFlavor(DishDto dishDto);

    // 分页查询所有的菜品并且展示
    public Page<Dish> queryDish(int page, int pageSize, String dishName);

    // 通过id查询dish属性据然后进行反显
    public DishDto queryById(Long id);

    // 更新菜品信息
    public void updateDish(DishDto dishDto);
}
