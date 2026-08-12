package com.demo.client;


import com.demo.config.FeignConfig;
import com.demo.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "Product-Service",configuration = FeignConfig.class)
//@FeignClient(url = "http://localhost:8083",value = "product-client")
public interface ProductClient {

    @GetMapping("/api/product/{id}")
    ProductDto getProductById(@PathVariable Long id);
}