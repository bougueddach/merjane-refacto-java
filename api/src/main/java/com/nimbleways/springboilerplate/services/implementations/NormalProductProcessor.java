package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NormalProductProcessor implements ProductProcessor {

    private final ProductService productService;

    @Override
    public ProductType getProductType() {
        return ProductType.NORMAL;
    }

    @Override
    public Optional<Product> processOrder(Product product) {
        if (product.isAvailable()) {
            product.reduceAvailabilityBy1();
            return Optional.of(product);
        } else if (product.hasLeadTime()) {
            productService.notifyDelay(product.getLeadTime(), product);
        }
        return Optional.empty();
    }

}
