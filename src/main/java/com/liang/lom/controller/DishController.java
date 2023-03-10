package com.liang.lom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liang.lom.common.R;
import com.liang.lom.dto.DishDto;
import com.liang.lom.entity.Category;
import com.liang.lom.entity.Dish;
import com.liang.lom.entity.DishFlavor;
import com.liang.lom.service.CategoryService;
import com.liang.lom.service.DishFlavorService;
import com.liang.lom.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto) {
        dishService.addDishAndFlavor(dishDto);

        // 清理某个分类下面的所有的菜品
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @param dishName
     * @return
     */
    @GetMapping("/page")
    public R<Page> queryDish(int page, int pageSize, String dishName) {
        // 先拷贝分页相关的属性除了records，然后单独拷贝records中的所有的属性
        Page<Dish> dishPage = dishService.queryDish(page, pageSize, dishName);
        // Page<DishDto> dishDtoPage = new Page<>();
        // 对象拷贝
        // BeanUtils.copyProperties(dishPage,dishDtoPage,"records");
        List<Dish> dishPageRecords = dishPage.getRecords();
        // List<DishDto> dishDtoList = null;
        // 父类的数组存储子类的对象是可以的，这个一种多态的体现
        dishPageRecords = dishPageRecords.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishPage.setRecords(dishPageRecords);
        return R.success(dishPage);
    }

    /**
     * 根据id查询菜品的信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> queryById(@PathVariable Long id) {
        DishDto dishDto = dishService.queryById(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto) {
        dishService.updateDish(dishDto);

        // 清理所有菜品的缓存
        // Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);

        // 清理某个分类下面的所有的菜品
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("修改菜品信息成功");
    }

    /**
     * 停售 --- 批量停售菜品
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> haltSales(@PathVariable int status, String id) {
        List<Dish> dishList = new ArrayList();
        log.info("id {}", id);
        // 存储id的数组
        String[] ids = id.split("\\,+");
        dishList = Arrays.stream(ids).map(item -> {
            Dish dish = new Dish();
            dish.setStatus(status);
            dish.setId(Long.valueOf(item));
            return dish;
        }).collect(Collectors.toList());
        dishService.updateBatchById(dishList);
        return R.success("所选菜品已停售");
    }

    /**
     * 删除 --- 批量删除数据
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteSales(String ids) {
        List<Dish> dishList = new ArrayList();
        String[] idList = ids.split("\\,+");
        dishList = Arrays.stream(idList).map(item -> {
            Dish dish = new Dish();
            dish.setIsDeleted(1);
            dish.setId(Long.valueOf(item));
            return dish;
        }).collect(Collectors.toList());

        dishService.updateBatchById(dishList);
        return R.success("菜品已经删除");
    }

    /*@GetMapping("list")
    public R<List<Dish>> getDistList(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 查询没有被删除的
        queryWrapper.eq(Dish::getIsDeleted, 0);
        // 查询没有停售的
        queryWrapper.eq(Dish::getStatus, 1);
        // 查询菜品分类下面的所有的菜品
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }*/

    /**
     * 移动端查询菜品列表
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> getDistList(Dish dish) {
        List<DishDto> dishDtoList;
        // 动态的构造key,每一个分类都会有一个独立的key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            // 如果存在,直接返回,无需查询数据库
            return R.success(dishDtoList);
        }
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 查询没有被删除的
        queryWrapper.eq(Dish::getIsDeleted, 0);
        // 查询没有停售的
        queryWrapper.eq(Dish::getStatus, 1);
        // 查询菜品分类下面的所有的菜品
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 获取口味表
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            // 设置菜品名称
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());

        // 如果不存在,需要查询数据库,将查询到的菜品放入redis中
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
