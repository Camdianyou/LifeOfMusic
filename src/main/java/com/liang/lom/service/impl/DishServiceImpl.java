package com.liang.lom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liang.lom.dto.DishDto;
import com.liang.lom.entity.Category;
import com.liang.lom.entity.Dish;
import com.liang.lom.entity.DishFlavor;
import com.liang.lom.mapper.DishMapper;
import com.liang.lom.service.CategoryService;
import com.liang.lom.service.DishFlavorService;
import com.liang.lom.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品同时保存对应的口味数据
     * 涉及多张表的操作
     *
     * @param dishDto
     */
    @Transactional
    public void addDishAndFlavor(DishDto dishDto) {
        // 保存菜品的基本信息
        this.save(dishDto);
        Long dishId = dishDto.getId(); // 菜品id

        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 往数组里面的每一个对象加一个元素
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        // 保存菜品口味的基本信息,由于口味偏好是数组需要我们进行批量存储
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 分页查询菜品数据
     *
     * @param page
     * @param pageSize
     * @param dishName
     * @return
     */
    @Override
    public Page<Dish> queryDish(int page, int pageSize, String dishName) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 查询未删除的对象
        queryWrapper.eq(Dish::getIsDeleted, 0);
        queryWrapper.like(dishName != null, Dish::getName, dishName);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        this.page(pageInfo, queryWrapper);
        return pageInfo;
    }

    /**
     * 通过id查询菜品信息进行修改数据的反显
     *
     * @param id
     * @return
     */
    @Override
    public DishDto queryById(Long id) {
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        // 获取口味的列表
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);

        // 创建dishDto对象进行拷贝作为返回值
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();

        // 深拷贝对象
        BeanUtils.copyProperties(dish, dishDto);
        // 获取Category对象通过getCategoryId
        Category category = categoryService.getById(dish.getCategoryId());
        // 设置categoryName、flavors
        dishDto.setCategoryName(category.getName());
        dishDto.setFlavors(dishFlavorList);
        return dishDto;
    }

    // 涉及多个表的操作需要做事务处理
    @Transactional
    public void updateDish(DishDto dishDto) {
        log.info("dishDto {}", dishDto);
        // 更新dish基本表
        this.updateById(dishDto);
        // 清空当前菜品对应的口味数据--dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        // 添加当前提交过来的口味信息
        // 获取dishId然后重新添加口味
        Long dishId = dishDto.getId();
        // 更新口味偏好
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 往数组里面的每一个对象加一个元素
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}
