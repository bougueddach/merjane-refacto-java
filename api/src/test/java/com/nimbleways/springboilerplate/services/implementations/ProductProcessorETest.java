package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class ProductProcessorETest {

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
    @Captor
    ArgumentCaptor<Set<Product>> productCaptor;

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

        Long orderId = orderService.processOrder(ORDER_ID);

        assertThat(ORDER_ID).isEqualTo(orderId);

        verify(productRepository, times(1)).saveAll(productCaptor.capture());
        Set<Product> savedProducts = productCaptor.getValue();

        Product usbCable = savedProducts.stream()
                .filter(p -> p.getName().equals("USB Cable"))
                .findFirst()
                .orElse(null);

        assertThat(usbCable).isNotNull();
        assertThat(usbCable.getAvailable()).isEqualTo(29);
    }

    @Test
    void processOrder_ShouldCallNotifyDelay_WhenNormalProductIsOutOfStock() {
        Product outOfStockProduct = new Product(null, 15, 0, NORMAL, "USB Dongle", null, null, null);
        mockOrder.setItems(Set.of(outOfStockProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        verify(productService, times(1)).notifyDelay(anyInt(), eq(outOfStockProduct));
        verify(productRepository, never()).saveAll(List.of(outOfStockProduct));
    }

    @Test
    void processOrder_ShouldHandleSeasonalProducts_WhenInSeason() {
        Product seasonalProduct = new Product(null, 10, 30, SEASONAL, "Watermelon", null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(58));
        mockOrder.setItems(Set.of(seasonalProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        verify(productRepository, times(1)).saveAll(productCaptor.capture());
        Set<Product> savedProducts = productCaptor.getValue();

        Product updatedProduct = savedProducts.stream()
                .filter(p -> p.getName().equals("Watermelon"))
                .findFirst()
                .orElse(null);

        assertThat(updatedProduct.getAvailable()).isEqualTo(29);
    }

    @Test
    void processOrder_ShouldHandleSeasonalProducts_WhenOutOfSeason() {
        Product outOfSeasonProduct = new Product(null, 10, 30, SEASONAL, "Grapes", null, LocalDate.now().plusDays(180), LocalDate.now().plusDays(240));
        mockOrder.setItems(Set.of(outOfSeasonProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        verify(productService, times(1)).handleSeasonalProduct(outOfSeasonProduct);
        verify(productRepository, never()).saveAll(List.of(outOfSeasonProduct)); // Should not reduce stock
    }

    @Test
    void processOrder_ShouldReduceStockForExpirableProducts_WhenNotExpired() {
        Product expirableProduct = new Product(null, 10, 30, EXPIRABLE, "Butter", LocalDate.now().plusDays(10), null, null);
        mockOrder.setItems(Set.of(expirableProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        verify(productRepository, times(1)).saveAll(productCaptor.capture());
        Set<Product> savedProducts = productCaptor.getValue();

        Product updatedProduct = savedProducts.stream()
                .filter(p -> p.getName().equals("Butter"))
                .findFirst()
                .orElse(null);

        assertThat(updatedProduct.getAvailable()).isEqualTo(29);
    }

    @Test
    void processOrder_ShouldHandleExpiredProducts_WhenExpired() {
        Product expiredProduct = new Product(null, 10, 30, EXPIRABLE, "Milk", LocalDate.now().minusDays(5), null, null);
        mockOrder.setItems(Set.of(expiredProduct));

        when(orderRepository.findByIdOrFail(ORDER_ID)).thenReturn(mockOrder);

        orderService.processOrder(ORDER_ID);

        verify(productService, times(1)).handleExpiredProduct(expiredProduct);
        verify(productRepository, never()).saveAll(List.of(expiredProduct)); // Should not reduce stock
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