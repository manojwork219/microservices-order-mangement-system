package com.example.inventoryservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/create")
    public ResponseEntity<Inventory> addInventory(@Valid @RequestBody Inventory inventory) {
        return new ResponseEntity<>(inventoryService.addInventory(inventory), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return new ResponseEntity<>(inventoryService.getAllInventory(), HttpStatus.OK);
    }

    
    @GetMapping("/{skuCode}")
    public ResponseEntity<Inventory> getInventory(@PathVariable String skuCode) {
        Inventory inventory = inventoryService.getInventoryBySkuCode(skuCode);
        
        return new ResponseEntity<>(inventory, HttpStatus.OK);
        
    }

    @PutMapping("/update/increment/{skuCode}")
    public ResponseEntity<String> updateInventoryQuantityIncrement(@PathVariable String skuCode, @RequestParam int quantity, @RequestParam double pricePerItem) {
        inventoryService.updateInventoryQuantity(skuCode, quantity, pricePerItem);  // Positive quantity to increment
        return new ResponseEntity<>("Inventory updated and stock incremented successfully for skuCode: " + skuCode, HttpStatus.OK);
    }

    @PutMapping("/update/decrement/{skuCode}")
    public ResponseEntity<String> updateInventoryQuantityDecrement(@PathVariable String skuCode, @RequestParam int quantity, @RequestParam double pricePerItem) {
        inventoryService.updateInventoryQuantity(skuCode, -quantity, pricePerItem);  // Negative quantity to decrement
        return new ResponseEntity<>("Inventory updated and stock decremented successfully for skuCode: " + skuCode, HttpStatus.OK);
    }
}
