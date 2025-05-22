package com.example.inventoryservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.inventoryservice.exception.InventoryInsufficientException;
import com.example.inventoryservice.exception.InventoryNotFoundException;
import com.example.inventoryservice.kafka.InventoryConfirmationProducer;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.service.impl.InventoryServiceImpl;

class InventoryServiceImplTest {

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryConfirmationProducer inventoryConfirmationProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddInventory() {
        Inventory inventory = new Inventory("sku001", 100, 10.0);

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        Inventory result = inventoryService.addInventory(inventory);

        assertNotNull(result);
        assertEquals("sku001", result.getSkuCode());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void testGetInventoryBySkuCode_found() {
        Inventory inventory = new Inventory("sku002", 50, 20.0);

        when(inventoryRepository.findBySkuCode("sku002")).thenReturn(inventory);

        Inventory result = inventoryService.getInventoryBySkuCode("sku002");

        assertEquals("sku002", result.getSkuCode());
        verify(inventoryRepository).findBySkuCode("sku002");
    }

    @Test
    void testGetInventoryBySkuCode_notFound() {
        when(inventoryRepository.findBySkuCode("invalidSku")).thenReturn(null);

        assertThrows(InventoryNotFoundException.class, () ->
            inventoryService.getInventoryBySkuCode("invalidSku")
        );

        verify(inventoryRepository).findBySkuCode("invalidSku");
    }

    @Test
    void testUpdateInventoryQuantity_increment_success() {
        Inventory inventory = new Inventory("sku003", 10, 15.0);

        when(inventoryRepository.findBySkuCode("sku003")).thenReturn(inventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        inventoryService.updateInventoryQuantity("sku003", 5, 17.0);

        assertEquals(15, inventory.getQuantity());
        assertEquals(17.0, inventory.getPricePerItem());
        verify(inventoryConfirmationProducer).sendConfirmation("CONFIRMED:sku003:-5");
    }

    @Test
    void testUpdateInventoryQuantity_decrement_success() {
        Inventory inventory = new Inventory("sku004", 20, 5.0);

        when(inventoryRepository.findBySkuCode("sku004")).thenReturn(inventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        inventoryService.updateInventoryQuantity("sku004", -10, 6.0);

        assertEquals(10, inventory.getQuantity());
        assertEquals(6.0, inventory.getPricePerItem());
        verify(inventoryConfirmationProducer).sendConfirmation("CONFIRMED:sku004:10");
    }

    @Test
    void testUpdateInventoryQuantity_notFound() {
        when(inventoryRepository.findBySkuCode("missingSku")).thenReturn(null);

        assertThrows(InventoryNotFoundException.class, () ->
            inventoryService.updateInventoryQuantity("missingSku", 5, 12.0)
        );

        verify(inventoryConfirmationProducer).sendConfirmation("REJECTED:missingSku:NOT_FOUND");
    }

    @Test
    void testUpdateInventoryQuantity_insufficientStock() {
        Inventory inventory = new Inventory("sku005", 3, 25.0);

        when(inventoryRepository.findBySkuCode("sku005")).thenReturn(inventory);

        assertThrows(InventoryInsufficientException.class, () ->
            inventoryService.updateInventoryQuantity("sku005", -5, 30.0)
        );

        verify(inventoryConfirmationProducer).sendConfirmation("REJECTED:sku005:INSUFFICIENT_STOCK");
    }

    @Test
    void testGetAllInventory() {
        List<Inventory> inventoryList = Arrays.asList(
            new Inventory("skuA", 10, 11.0),
            new Inventory("skuB", 20, 22.0)
        );

        when(inventoryRepository.findAll()).thenReturn(inventoryList);

        List<Inventory> result = inventoryService.getAllInventory();

        assertEquals(2, result.size());
        verify(inventoryRepository).findAll();
    }
}
