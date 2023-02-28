package com.liang.lom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liang.lom.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);
}
