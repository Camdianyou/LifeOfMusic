package com.liang.lom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liang.lom.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

}