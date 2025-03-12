package com.readrealm.order.mapper;

import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.model.Order;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

        @Mapping(target = "orderId", ignore = true)
        Order toOrder(OrderRequest orderRequest);

        @Mapping(target = "paymentDetails", source = "paymentResponse")
        @Mapping(target = "orderId", source = "order.orderId")
        @Mapping(target = "createdDate", source = "order.createdDate")
        @Mapping(target = "updatedDate", source = "order.updatedDate")
        OrderResponse toOrderResponse(Order order, PaymentResponse paymentResponse);

        OrderResponse toOrderResponse(Order order);

        List<OrderResponse> toOrderResponseList(List<Order> orders);
}