package com.example.inventoryservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "skuCode cannot be null")
    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;

    @Min(value = 0, message = "Quantity must be non-negative")
    @Column(name = "quantity")
    private int quantity;

    @NotNull(message = "pricePerItem cannot be null")
    @Min(value = 0, message = "pricePerItem must be a positive value")
    @Column(name = "pricePerItem", nullable = false)
    private double pricePerItem;

    public Inventory() {}

    public Inventory(String skuCode, int quantity, double pricePerItem) {
        this.skuCode = skuCode;
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
    }

}
