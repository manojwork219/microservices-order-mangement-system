package com.example.orderservice.exception;

public class InventoryInsufficientException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
    public InventoryInsufficientException(String message) {
        super(message);
    }
}
