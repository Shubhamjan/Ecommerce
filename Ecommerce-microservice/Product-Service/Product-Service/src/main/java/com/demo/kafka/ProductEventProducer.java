//package com.demo.kafka;
//
//import com.demo.event.ProductCreatedEvent;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class ProductEventProducer {
//
//    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
//
//
//    public ProductEventProducer(KafkaTemplate<String,ProductCreatedEvent> kafkaTemplate){
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    public void sendProductQuantity(ProductCreatedEvent event){
//
//        kafkaTemplate.send("product-event",event.getProductId().toString(),event);
//    }
//}
