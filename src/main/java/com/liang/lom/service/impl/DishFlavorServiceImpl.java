package com.liang.lom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liang.lom.entity.DishFlavor;
import com.liang.lom.mapper.DishFlavorMapper;
import com.liang.lom.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
