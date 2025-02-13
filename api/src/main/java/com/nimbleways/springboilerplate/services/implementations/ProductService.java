package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    public void handleSeasonalProduct(Product product) {
        if (product.willBeOutOfSeason()) {
            sendOutOfStockAndSave(product);
        } else if (product.isSeasonNotStarted()) {
            sendOutOfStockAndSave(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    public void handleExpiredProduct(Product product) {
        if (product.isAvailable() && product.isStillValid()) {
            product.reduceAvailabilityBy1();
            productRepository.save(product);
        } else {
            sendExpirationAndSave(product);
        }
    }

    private void sendOutOfStockAndSave(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        product.setAvailable(0);
        productRepository.save(product);
    }

    private void sendExpirationAndSave(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }
}
