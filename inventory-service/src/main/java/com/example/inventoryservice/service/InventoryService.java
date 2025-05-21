package com.example.inventoryservice.service;

import java.util.List;

import com.example.inventoryservice.model.Inventory;

public interface InventoryService {
    Inventory addInventory(Inventory inventory);
    Inventory getInventoryBySkuCode(String skuCode);
    void updateInventoryQuantity(String skuCode, int quantity, double pricePerItem);
    List<Inventory> getAllInventory();

}
