package com.readrealm.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/v1/orders/confirm")
    void confirmOrder(@RequestParam String orderId);

}