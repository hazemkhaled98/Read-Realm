package com.readrealm.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "orderClient", url = "http://localhost:8081/v1/orders")
public interface OrderClient {

    @PostMapping("/confirm")
    void confirmOrder(@RequestParam String orderId);

}