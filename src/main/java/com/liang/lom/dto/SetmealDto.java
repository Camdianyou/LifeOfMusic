package com.liang.lom.dto;

import com.liang.lom.entity.Setmeal;
import com.liang.lom.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
