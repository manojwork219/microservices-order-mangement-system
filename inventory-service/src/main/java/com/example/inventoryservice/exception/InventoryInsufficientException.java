package com.example.inventoryservice.exception;

public class InventoryInsufficientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InventoryInsufficientException(String message) {
		super(message);
	}
}
