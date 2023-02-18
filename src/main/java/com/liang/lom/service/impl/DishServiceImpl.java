package com.liang.lom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liang.lom.entity.Dish;
import com.liang.lom.mapper.DishMapper;
import com.liang.lom.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
}
