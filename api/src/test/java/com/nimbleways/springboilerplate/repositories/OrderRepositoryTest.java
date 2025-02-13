package com.nimbleways.springboilerplate.repositories;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.exceptions.OrderDoesNotExistException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest // ✅ Loads only JPA-related components, faster than @SpringBootTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository; // ✅ Now testing the real repository

    @Test
    void getByIdOrThrow_ShouldReturnOrder_WhenOrderExists() {
        // Arrange: Create and save an order in the test database
        Order order = new Order();
        order = orderRepository.save(order); // ✅ Save it so we can retrieve it

        // Act
        Order result = orderRepository.findByIdOrFail(order.getId());

        // Assert
        assertThat(result).isNotNull().isEqualTo(order);
    }

    @Test
    void getByIdOrThrow_ShouldThrowException_WhenOrderNotFound() {
        // Arrange: Use a random UUID that doesn’t exist
        Long nonExistentOrderId = 999L;

        // Act & Assert
        assertThatThrownBy(() -> orderRepository.findByIdOrFail(nonExistentOrderId))
                .isInstanceOf(OrderDoesNotExistException.class)
                .hasMessageContaining("Order with ID " + nonExistentOrderId + " does not exist.");
    }
}