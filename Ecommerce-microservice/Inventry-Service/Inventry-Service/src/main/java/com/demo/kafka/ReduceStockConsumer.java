package com.demo.kafka;

import com.demo.dto.InventoryRequest;
import com.demo.event.ProductCreatedEvent;
import com.demo.event.StockUpdateEvent;
import com.demo.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReduceStockConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "REDUCE-STOCK",
            containerFactory = "reduceStockListenerFactory"
    )
    public void consume(StockUpdateEvent event,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📦 Received event | productId={} | quantity={} | partition={} | offset={}",
                event.getProductId(), event.getQuantity(), partition, offset);

//        inventoryService.updateStock(event);
        InventoryRequest inventoryRequest = new InventoryRequest();
        inventoryRequest.setProductId(event.getProductId());
        inventoryRequest.setQuantity(event.getQuantity());
        inventoryRequest.setOperation("decrease");
        inventoryService.updateStock(inventoryRequest);
    }
}
