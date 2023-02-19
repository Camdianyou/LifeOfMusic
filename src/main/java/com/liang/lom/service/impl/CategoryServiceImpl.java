package com.liang.lom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liang.lom.common.CustomException;
import com.liang.lom.entity.Category;
import com.liang.lom.entity.Dish;
import com.liang.lom.entity.Setmeal;
import com.liang.lom.mapper.CategoryMapper;
import com.liang.lom.service.CategoryService;
import com.liang.lom.service.DishService;
import com.liang.lom.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类
     *
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 查询当前分类是否关联了菜品,如果关联了,抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 根据分类id进行查询,且查询出来的菜品没有被删除
        dishLambdaQueryWrapper.eq(Dish::getIsDeleted,0);
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);
        if (dishCount > 0) {
            // 已经关联了菜品,抛出一个业务异常
            throw new CustomException("当前分类下存在菜品,不能删除");
        }

        // 查询当前分类是否关联了套餐,如果关联了,抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        if (setmealCount > 0) {
            // 已经关联了套餐,抛出一个业务异常
            throw new CustomException("当前分类下存在套餐,不能删除");
        }
        // 正常删除分类
        // 将is_deleted标签改为1就代表已删除
        Category category = super.getById(id);
        category.setIsDeleted(1);
        super.updateById(category);
    }
}
