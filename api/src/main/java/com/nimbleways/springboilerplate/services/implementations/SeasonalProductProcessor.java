package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeasonalProductProcessor implements ProductProcessor{

    private final ProductService productService;

    @Override
    public ProductType getProductType() {
        return ProductType.SEASONAL;
    }

    @Override
    public Optional<Product> processOrder(Product product) {
        if (product.isInSeason() && product.isAvailable()) {
            product.reduceAvailabilityBy1();
            return Optional.of(product);
        } else {
            productService.handleSeasonalProduct(product);
            return Optional.empty();
        }
    }
}
