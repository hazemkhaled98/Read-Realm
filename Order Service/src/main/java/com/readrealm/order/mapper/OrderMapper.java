package com.readrealm.order.mapper;

import com.readrealm.auth.model.SecurityPrincipal;
import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.order.model.Order;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

        @Mapping(target = "orderId", ignore = true)
        @Mapping(target = "userId", ignore = true)
        Order toOrder(OrderRequest orderRequest);

        @Mapping(target = "paymentDetails", source = "paymentResponse")
        @Mapping(target = "orderId", source = "order.orderId")
        @Mapping(target = "createdDate", source = "order.createdDate")
        @Mapping(target = "updatedDate", source = "order.updatedDate")
        OrderResponse toOrderResponse(Order order, PaymentResponse paymentResponse);

        OrderResponse toOrderResponse(Order order);

        List<OrderResponse> toOrderResponseList(List<Order> orders);

        @Mapping(target = "userFirstName", source = "principal.firstName")
        @Mapping(target = "userLastName", source = "principal.lastName")
        @Mapping(target = "userEmail", source = "principal.email")
        OrderEvent toOrderEvent(Order order, SecurityPrincipal principal);
}