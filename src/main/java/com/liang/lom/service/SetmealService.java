package com.liang.lom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liang.lom.common.R;
import com.liang.lom.dto.SetmealDto;
import com.liang.lom.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void addSetmeal(SetmealDto setmealDto);

    public void removeSetmeal(List<Long> ids);

}
