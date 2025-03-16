package com.readrealm.payment.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

public interface OrderClient {

    @PostExchange("/v1/orders/confirm")
    void confirmOrder(@RequestParam String orderId);

}