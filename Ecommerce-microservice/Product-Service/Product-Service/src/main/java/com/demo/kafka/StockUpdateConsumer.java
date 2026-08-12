package com.demo.kafka;

import com.demo.event.ProductCreatedEvent;
import com.demo.event.StockUpdateEvent;
import com.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockUpdateConsumer {

    private final ProductService productService;

    @KafkaListener(
            topics = "Update-Stock",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(StockUpdateEvent event,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📦 Received event | productId={} | quantity={} | partition={} | offset={}",
                event.getProductId(), event.getQuantity(), partition, offset);

       productService.UpdateProductQuantity(event);
    }
}
