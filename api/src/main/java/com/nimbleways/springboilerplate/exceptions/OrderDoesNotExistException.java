package com.nimbleways.springboilerplate.exceptions;

import java.util.NoSuchElementException;

public class OrderDoesNotExistException extends NoSuchElementException {
    public OrderDoesNotExistException(Long orderId) {
        super("Order with ID " + orderId + " does not exist.");
    }
}
