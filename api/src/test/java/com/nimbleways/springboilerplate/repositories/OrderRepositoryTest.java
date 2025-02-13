package com.nimbleways.springboilerplate.repositories;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.exceptions.OrderDoesNotExistException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest 
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void getByIdOrThrow_ShouldReturnOrder_WhenOrderExists() {
        Order order = new Order();
        order = orderRepository.save(order);

        Order result = orderRepository.findByIdOrFail(order.getId());

        assertThat(result).isNotNull().isEqualTo(order);
    }

    @Test
    void getByIdOrThrow_ShouldThrowException_WhenOrderNotFound() {
        Long nonExistentOrderId = 999L;

        assertThatThrownBy(() -> orderRepository.findByIdOrFail(nonExistentOrderId))
                .isInstanceOf(OrderDoesNotExistException.class)
                .hasMessageContaining("Order with ID " + nonExistentOrderId + " does not exist.");
    }
}