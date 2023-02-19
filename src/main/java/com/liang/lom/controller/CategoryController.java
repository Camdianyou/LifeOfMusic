package com.liang.lom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liang.lom.common.R;
import com.liang.lom.entity.Category;
import com.liang.lom.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     *
     * @param category
     * @return
     */
    @PostMapping
    public R<String> addType(@RequestBody Category category) {
        log.info("Category {} ", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> queryTypeByPage(int page, int pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Category::getIsDeleted, 0);
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类
     *
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> deleteType(Long id) {
        categoryService.remove(id);
        return R.success("分类信息删除成功");
    }

    /**
     * 根据id修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping
    public R<String> updateType(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 获取菜品分类
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    // 如果传递的是某一个属性的话，我们基本上使用一个对象来接受这个属性
    public R<List<Category>> getTypeList(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 获取对应type的菜品
        queryWrapper.eq(Category::getType, category.getType());
        // 添加排序条件,如果sort相同我们就通过更新时间来排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        // 获取没删除的菜品
        queryWrapper.eq(Category::getIsDeleted, 0);
        List<Category> typeList = categoryService.list(queryWrapper);
        return R.success(typeList);
    }

}
