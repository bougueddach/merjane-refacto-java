package com.nimbleways.springboilerplate.repositories;

import com.nimbleways.springboilerplate.exceptions.OrderDoesNotExistException;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nimbleways.springboilerplate.entities.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findById(Long orderId);
    default Order findByIdOrFail(Long orderId) {
        return findById(orderId)
                .orElseThrow(() -> new OrderDoesNotExistException(orderId)); // ✅ Automatically throws exception
    }

}
