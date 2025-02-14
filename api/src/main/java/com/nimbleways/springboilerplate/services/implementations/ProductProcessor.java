package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;

import java.util.Optional;

public interface ProductProcessor {
    ProductType getProductType();
    Optional<Product> processOrder(Product product);
}
