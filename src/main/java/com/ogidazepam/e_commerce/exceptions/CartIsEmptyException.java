package com.ogidazepam.e_commerce.exceptions;

public class CartIsEmptyException extends RuntimeException {
    public CartIsEmptyException(String message) {
        super(message);
    }
}
