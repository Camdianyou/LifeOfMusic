package com.liang.lom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liang.lom.common.R;
import com.liang.lom.dto.SetmealDto;
import com.liang.lom.entity.SetmealDish;
import com.liang.lom.mapper.SetmealDishMapper;
import com.liang.lom.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
