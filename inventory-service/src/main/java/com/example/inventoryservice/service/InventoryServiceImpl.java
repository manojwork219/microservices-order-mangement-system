package com.example.inventoryservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.inventoryservice.exception.InventoryInsufficientException;
import com.example.inventoryservice.exception.InventoryNotFoundException;
import com.example.inventoryservice.kafka.InventoryConfirmationProducer;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

	@Autowired
	InventoryConfirmationProducer inventoryConfirmationProducer;
	
    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public Inventory addInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory getInventoryBySkuCode(String skuCode) {
    	Inventory inventory = inventoryRepository.findBySkuCode(skuCode);
    	 if (inventory != null) {
             return inventory;
         }
    	 else {
             throw new InventoryNotFoundException("Inventory not found for skuCode: " + skuCode);
         }
    }

    @Override
    public void updateInventoryQuantity(String skuCode, int quantityChange, double pricePerItem) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode);
        if (inventory != null) {
            int updatedQuantity = inventory.getQuantity() + quantityChange;

            if (updatedQuantity >= 0) {
                inventory.setQuantity(updatedQuantity);
                inventory.setPricePerItem(pricePerItem); // Optional update
                inventoryRepository.save(inventory);

                System.out.println("✅ Inventory updated for skuCode: " + skuCode);

                // ✅ Send confirmation to order-service
                String confirmationMessage = "CONFIRMED:" + skuCode + ":" + (quantityChange*-1);
                inventoryConfirmationProducer.sendConfirmation(confirmationMessage);

            } else {
                System.err.println("❌ Insufficient inventory for skuCode: " + skuCode);
                
                // ❌ Send rejection message
                String rejectionMessage = "REJECTED:" + skuCode + ":INSUFFICIENT_STOCK";
                inventoryConfirmationProducer.sendConfirmation(rejectionMessage);
                
                throw new InventoryInsufficientException("Insufficient inventory for skuCode: " + skuCode);
            }

        } else {
            System.err.println("❌ Inventory not found for skuCode: " + skuCode);

            // ❌ Send rejection message
            String rejectionMessage = "REJECTED:" + skuCode + ":NOT_FOUND";
            inventoryConfirmationProducer.sendConfirmation(rejectionMessage);
            
            throw new InventoryNotFoundException("Inventory not found for skuCode: " + skuCode);
        }
    }


    @Override
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

}
