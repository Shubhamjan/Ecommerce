//package com.demo.service;
//
//import com.demo.entity.Inventory;
//import com.demo.event.ProductCreatedEvent;
//import com.demo.repository.InventoryRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//public class InventoryConsumer {
//
//    @Autowired
//    private InventoryRepository inventoryRepository;
//
//    @KafkaListener(topics ="product-created-topic",groupId = "inventory-group" )
//    public void consume(ProductCreatedEvent event){
//        Inventory inventory = new Inventory();
//
//        inventory.setProductId(event.getProductId());
//        inventory.setQuantity(event.getQuantity());
//
//        Inventory in = inventoryRepository.save(inventory);
//
//        if(in!=null){
//            System.out.println("✅ Inventory created for product: " + event.getProductId());
//        }
//    }
//
//}
