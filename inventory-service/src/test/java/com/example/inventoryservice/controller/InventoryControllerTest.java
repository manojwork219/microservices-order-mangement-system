package com.example.inventoryservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddInventory() throws Exception {
        Inventory inventory = new Inventory("SKU123", 10, 100.0);
        Mockito.when(inventoryService.addInventory(any(Inventory.class))).thenReturn(inventory);

        mockMvc.perform(post("/api/inventory/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skuCode").value("SKU123"));
    }

    @Test
    void testGetAllInventory() throws Exception {
        List<Inventory> inventoryList = Arrays.asList(
                new Inventory("SKU123", 10, 100.0),
                new Inventory("SKU456", 5, 50.0)
        );
        Mockito.when(inventoryService.getAllInventory()).thenReturn(inventoryList);

        mockMvc.perform(get("/api/inventory/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void testGetInventoryBySkuCode() throws Exception {
        Inventory inventory = new Inventory("SKU123", 10, 100.0);
        Mockito.when(inventoryService.getInventoryBySkuCode("SKU123")).thenReturn(inventory);

        mockMvc.perform(get("/api/inventory/SKU123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuCode").value("SKU123"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void testUpdateInventoryQuantityIncrement() throws Exception {
        mockMvc.perform(put("/api/inventory/update/increment/SKU123")
                .param("quantity", "5")
                .param("pricePerItem", "110.0"))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventory updated and stock incremented successfully for skuCode: SKU123"));

        Mockito.verify(inventoryService).updateInventoryQuantity("SKU123", 5, 110.0);
    }

    @Test
    void testUpdateInventoryQuantityDecrement() throws Exception {
        mockMvc.perform(put("/api/inventory/update/decrement/SKU123")
                .param("quantity", "3")
                .param("pricePerItem", "90.0"))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventory updated and stock decremented successfully for skuCode: SKU123"));

        Mockito.verify(inventoryService).updateInventoryQuantity("SKU123", -3, 90.0);
    }
}
