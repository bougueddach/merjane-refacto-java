package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nimbleways.springboilerplate.entities.ProductType.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    public static final long ORDER_ID = 1L;
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Order mockOrder;
    private Set<Product> mockProducts;

    @BeforeEach
    void setup() {
        mockProducts = new HashSet<>(createProducts());
        mockOrder = new Order();
        mockOrder.setId(ORDER_ID);
        mockOrder.setItems(mockProducts);
    }

    @Test
    void processOrder_ShouldReduceStockForNormalProducts_WhenAvailable() throws Exception {
        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        ProcessOrderResponse response = orderService.processOrder(ORDER_ID);

        assertThat(response).isNotNull();
        assertThat(ORDER_ID).isEqualTo(response.id());

        for (Product product : mockProducts) {
            if (product.getType().equals(NORMAL) && product.getAvailable() > 0) {
                assertThat(product.getAvailable()).isEqualTo(29);

                verify(productRepository, times(1)).save(product);
            }
        }
    }

    @Test
    void processOrder_ShouldCallNotifyDelay_WhenNormalProductIsOutOfStock() {
        Product outOfStockProduct = new Product(null, 15, 0, NORMAL, "USB Dongle", null, null, null);
        mockOrder.setItems(Set.of(outOfStockProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        verify(productService, times(1)).notifyDelay(anyInt(), eq(outOfStockProduct));
        verify(productRepository, never()).save(outOfStockProduct); // Shouldn't save if no stock
    }

    @Test
    void processOrder_ShouldHandleSeasonalProducts_WhenInSeason() {
        Product seasonalProduct = new Product(null, 10, 30, SEASONAL, "Watermelon", null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(58));
        mockOrder.setItems(Set.of(seasonalProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        assertThat(seasonalProduct.getAvailable()).isEqualTo(29); // Stock should be reduced
        verify(productRepository, times(1)).save(seasonalProduct);
    }

    @Test
    void processOrder_ShouldHandleSeasonalProducts_WhenOutOfSeason() {
        Product outOfSeasonProduct = new Product(null, 10, 30, SEASONAL, "Grapes", null, LocalDate.now().plusDays(180), LocalDate.now().plusDays(240));
        mockOrder.setItems(Set.of(outOfSeasonProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        verify(productService, times(1)).handleSeasonalProduct(outOfSeasonProduct);
        verify(productRepository, never()).save(outOfSeasonProduct); // Should not reduce stock
    }

    @Test
    void processOrder_ShouldReduceStockForExpirableProducts_WhenNotExpired() {
        Product expirableProduct = new Product(null, 10, 30, EXPIRABLE, "Butter", LocalDate.now().plusDays(10), null, null);
        mockOrder.setItems(Set.of(expirableProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        assertThat(expirableProduct.getAvailable()).isEqualTo(29); // Stock reduced
        verify(productRepository, times(1)).save(expirableProduct);
    }

    @Test
    void processOrder_ShouldHandleExpiredProducts_WhenExpired() {
        Product expiredProduct = new Product(null, 10, 30, EXPIRABLE, "Milk", LocalDate.now().minusDays(5), null, null);
        mockOrder.setItems(Set.of(expiredProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        verify(productService, times(1)).handleExpiredProduct(expiredProduct);
        verify(productRepository, never()).save(expiredProduct); // Should not reduce stock
    }

    private List<Product> createProducts() {
        return Arrays.asList(
                new Product(null, 15, 30, NORMAL, "USB Cable", null, null, null),
                new Product(null, 10, 0, NORMAL, "USB Dongle", null, null, null),
                new Product(null, 15, 30, EXPIRABLE, "Butter", LocalDate.now().plusDays(26), null, null),
                new Product(null, 90, 6, EXPIRABLE, "Milk", LocalDate.now().minusDays(2), null, null),
                new Product(null, 15, 30, SEASONAL, "Watermelon", null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(58)),
                new Product(null, 15, 30, SEASONAL, "Grapes", null, LocalDate.now().plusDays(180), LocalDate.now().plusDays(240))
        );
    }
}