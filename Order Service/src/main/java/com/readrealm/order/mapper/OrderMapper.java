package com.readrealm.order.mapper;

import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

        @Mapping(target = "orderId", ignore = true)
        Order toOrder(OrderRequest orderRequest);

        @Mapping(target = "orderRequest.userId", source = "userId")
        @Mapping(target = "orderRequest.totalAmount", source = "totalAmount")
        @Mapping(target = "orderRequest.details", source = "details")
        @Mapping(target = "orderId", source = "orderId")
        OrderResponse toOrderResponse(Order order);


        List<OrderResponse> toOrderResponseList(List<Order> orders);
}