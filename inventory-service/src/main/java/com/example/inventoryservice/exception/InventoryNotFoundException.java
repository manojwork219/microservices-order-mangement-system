package com.example.inventoryservice.exception;

public class InventoryNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InventoryNotFoundException(String message) {
		super(message);
	}
}
