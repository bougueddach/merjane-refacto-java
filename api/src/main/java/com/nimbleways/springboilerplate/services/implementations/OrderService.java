package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findByIdOrFail(orderId);


        Set<Product> updatedProducts = new HashSet<>();
        for (Product product : order.getItems()) {
            Product processedProduct = processProduct(product);
            if (processedProduct != null) {

                updatedProducts.add(processedProduct);
            }
        }
        if (!updatedProducts.isEmpty()) {
            productRepository.saveAll(updatedProducts);
        }

        return new ProcessOrderResponse(order.getId());
    }

    private Product processProduct(Product product) {
        switch (product.getType()) {
            case NORMAL -> {
                return processNormalProduct(product);
            }
            case SEASONAL -> {
                return processSeasonalProduct(product);
            }
            case EXPIRABLE -> {
                return processExpirableProduct(product);
            }
        }
        return null;
    }

    private Product processNormalProduct(Product product) {
        if (product.isAvailable()) {
            product.reduceAvailabilityBy1();
            return product;
        } else if (product.hasLeadTime()) {
            productService.notifyDelay(product.getLeadTime(), product);
        }
        return null;
    }

    private Product processSeasonalProduct(Product product) {
        if (isInSeason(product) && product.isAvailable()) {
            product.reduceAvailabilityBy1();
            return product;
        } else {
            productService.handleSeasonalProduct(product);
            return null;
        }
    }

    private Product processExpirableProduct(Product product) {
        if (product.isAvailable() && product.getExpiryDate().isAfter(LocalDate.now())) {
            product.reduceAvailabilityBy1();
            return product;

        } else {
            productService.handleExpiredProduct(product);
            return null;
        }
    }

    private boolean isInSeason(Product product) {
        return LocalDate.now().isAfter(product.getSeasonStartDate()) &&
                LocalDate.now().isBefore(product.getSeasonEndDate());
    }
}
