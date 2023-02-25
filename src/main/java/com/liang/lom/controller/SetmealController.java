package com.liang.lom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liang.lom.common.R;
import com.liang.lom.dto.SetmealDto;
import com.liang.lom.entity.Category;
import com.liang.lom.entity.Dish;
import com.liang.lom.entity.Setmeal;
import com.liang.lom.service.CategoryService;
import com.liang.lom.service.SetmealDishService;
import com.liang.lom.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    private R<String> addSetmeal(@RequestBody SetmealDto setmealDto) {
        setmealService.addSetmeal(setmealDto);
        return R.success("套餐添加成功");
    }

    /**
     * 套餐的分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    private R<Page> getSetmealByPage(int page, int pageSize, String name) {
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        // 条件设置
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 根据套餐名称的模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        // 查询未删除的套餐
        queryWrapper.eq(Setmeal::getIsDeleted, 0);
        // 根据更新时间降序排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, queryWrapper);
        // 创建一个套餐数组
        List<Setmeal> setmeals = new ArrayList<>();
        // 将套餐的类型名称获取传递给前端
        setmeals = setmealPage.getRecords().stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());
        // 重新设置套餐的records
        setmealPage.setRecords(setmeals);
        return R.success(setmealPage);
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> removeSetmeal(@RequestParam List<Long> ids) {
        setmealService.removeSetmeal(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 停售启售套餐
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> haltSell(@PathVariable int status, @RequestParam List<Long> ids) {
        List<Setmeal> setmealArrayList = new ArrayList<>();
        setmealArrayList = setmealService.listByIds(ids).stream().peek(item -> item.setStatus(status)).collect(Collectors.toList());

        setmealService.updateBatchById(setmealArrayList);

        return R.success("状态更新成功");
    }
}
