package com.liang.lom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liang.lom.common.R;
import com.liang.lom.dto.OrdersDto;
import com.liang.lom.entity.OrderDetail;
import com.liang.lom.entity.Orders;
import com.liang.lom.service.OrderDetailService;
import com.liang.lom.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/userPage")
    public R<List<OrdersDto>> queryByPage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        orderService.page(pageInfo);
        List<OrdersDto> ordersDtoArrayList;
        ordersDtoArrayList = pageInfo.getRecords().stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> list = orderDetailService.list(orderDetailLambdaQueryWrapper);
            ordersDto.setOrderDetails(list);
            return ordersDto;
        }).collect(Collectors.toList());
        return R.success(ordersDtoArrayList);
    }
}