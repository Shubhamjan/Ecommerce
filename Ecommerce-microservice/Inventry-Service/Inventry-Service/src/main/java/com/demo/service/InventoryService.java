package com.demo.service;

import com.demo.dto.InventoryRequest;
import com.demo.dto.InventoryResponse;
import com.demo.entity.Inventory;

import com.demo.event.StockUpdateEvent;
import com.demo.exception.InventoryNotFoundException;
import com.demo.mapper.InventoryMapper;
import com.demo.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private final InventoryMapper inventoryMapper;

    private final KafkaTemplate<String, StockUpdateEvent> kafkaTemplate;

    public Boolean checkInventory(Long productId,int quantity) {

        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow(() -> new InventoryNotFoundException(productId));

        return inventory.getQuantity() > quantity;
    }

    public void addInventory(InventoryRequest request) {
        Inventory inventory = inventoryMapper.toEntity(request);

        Inventory saved = inventoryRepository.save(inventory);

    }

    public InventoryResponse updateStock(InventoryRequest inventoryRequest) {

        Inventory inventory = inventoryRepository.findByProductId(inventoryRequest.getProductId()).orElseThrow(()->new InventoryNotFoundException(inventoryRequest.getProductId()));

        Integer availableQuantity = inventory.getQuantity();

        if(inventoryRequest.getOperation()!=null && inventoryRequest.getOperation().equalsIgnoreCase("Increase")){
            inventory.setQuantity(availableQuantity+inventoryRequest.getQuantity());
        }
        if(inventoryRequest.getOperation()!=null && inventoryRequest.getOperation().equalsIgnoreCase("Decrease")){
            if(inventory.getQuantity()<inventoryRequest.getQuantity()){
                throw new RuntimeException("Stock reduction not possible as request reduction is more than stock");
            }else{
                inventory.setQuantity(availableQuantity-inventoryRequest.getQuantity());
            }

        }

        Inventory saved = inventoryRepository.save(inventory);

        StockUpdateEvent event = new StockUpdateEvent(saved.getProductId(),saved.getQuantity());

        kafkaTemplate.send("Update-Stock", event.getProductId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Failed to publish event for productId={}",
                                event.getProductId(), ex);
                    } else {
                        log.info("✅ Update stock :- Event published | productId={} | partition={} | offset={}",
                                event.getProductId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });

        return inventoryMapper.toResponse(saved);
    }
}
