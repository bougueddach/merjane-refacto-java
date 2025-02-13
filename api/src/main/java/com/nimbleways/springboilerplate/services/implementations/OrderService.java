package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findByIdOrFail(orderId);

        Set<Product> updatedProducts = order.getItems().stream()
                .map(this::processProduct)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        if (!updatedProducts.isEmpty()) {
            productRepository.saveAll(updatedProducts); // batch save
        }

        return new ProcessOrderResponse(order.getId());
    }

    private Optional<Product> processProduct(Product product) {
        return switch (product.getType()) {
            case NORMAL -> processNormalProduct(product);
            case SEASONAL -> processSeasonalProduct(product);
            case EXPIRABLE -> processExpirableProduct(product);
        };
    }

    private Optional<Product> processNormalProduct(Product product) {
        if (product.isAvailable()) {
            product.reduceAvailabilityBy1();
            return Optional.of(product);
        } else if (product.hasLeadTime()) {
            productService.notifyDelay(product.getLeadTime(), product);
        }
        return Optional.empty();
    }

    private Optional<Product> processSeasonalProduct(Product product) {
        if (product.isInSeason() && product.isAvailable()) {
            product.reduceAvailabilityBy1();
            return Optional.of(product);
        } else {
            productService.handleSeasonalProduct(product);
            return Optional.empty();
        }
    }

    private Optional<Product> processExpirableProduct(Product product) {
        if (product.isAvailable() && product.getExpiryDate().isAfter(LocalDate.now())) {
            product.reduceAvailabilityBy1();
            return Optional.of(product);

        } else {
            productService.handleExpiredProduct(product);
            return Optional.empty();
        }
    }
}
