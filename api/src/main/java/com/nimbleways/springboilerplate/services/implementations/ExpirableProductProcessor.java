package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpirableProductProcessor implements ProductProcessor{

    private final ProductService productService;

    @Override
    public ProductType getProductType() {
        return ProductType.EXPIRABLE;
    }

    @Override
    public Optional<Product> processOrder(Product product) {
        if (product.isAvailable() && product.getExpiryDate().isAfter(LocalDate.now())) {
            product.reduceAvailabilityBy1();
            return Optional.of(product);
        } else {
            productService.handleExpiredProduct(product);
            return Optional.empty();
        }
    }
}
