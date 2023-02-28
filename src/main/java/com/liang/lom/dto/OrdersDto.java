package com.liang.lom.dto;

import com.liang.lom.entity.OrderDetail;
import com.liang.lom.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
