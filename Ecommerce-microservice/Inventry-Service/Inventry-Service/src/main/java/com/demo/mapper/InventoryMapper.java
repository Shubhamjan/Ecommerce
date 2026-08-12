package com.demo.mapper;


import com.demo.dto.InventoryRequest;
import com.demo.dto.InventoryResponse;
import com.demo.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public Inventory toEntity(InventoryRequest request){

        Inventory inventory = new Inventory();
//        inventory.setSkuCode(request.getSkuCode());
        inventory.setProductId(request.getProductId());
        inventory.setQuantity(request.getQuantity());
        return inventory;
    }

    public InventoryResponse toResponse(Inventory inventory){
        return new InventoryResponse(
//                inventory.getSkuCode(),
                inventory.getProductId(),
                inventory.getQuantity()>0,
                inventory.getQuantity()
        );
    }
}
