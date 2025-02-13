package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();

        System.out.println(order);
        List<Long> ids = new ArrayList<>();
        ids.add(orderId);
        Set<Product> products = order.getItems();

        for (Product p : products) {
            if (p.getType().equals(ProductType.NORMAL)) {
                if (p.getAvailable() > 0) {
                    p.setAvailable(p.getAvailable() - 1);
                    productRepository.save(p);
                } else {
                    int leadTime = p.getLeadTime();
                    if (leadTime > 0) {
                        productService.notifyDelay(leadTime, p);
                    }
                }
            } else if (p.getType().equals(ProductType.SEASONAL)) {
                if ((LocalDate.now().isAfter(p.getSeasonStartDate()) && LocalDate.now().isBefore(p.getSeasonEndDate())
                        && p.getAvailable() > 0)) {
                    p.setAvailable(p.getAvailable() - 1);
                    productRepository.save(p);
                } else {
                    productService.handleSeasonalProduct(p);
                }
            } else if (p.getType().equals(ProductType.EXPIRABLE)) {
                if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
                    p.setAvailable(p.getAvailable() - 1);
                    productRepository.save(p);
                } else {
                    productService.handleExpiredProduct(p);
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }
}
