package com.demo.client;

import com.demo.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "Order-Service")
public interface OrderClient {


    @GetMapping("/api/order/{orderNumber}")
    OrderDto getOrderByNumber(@PathVariable String orderNumber);
}
