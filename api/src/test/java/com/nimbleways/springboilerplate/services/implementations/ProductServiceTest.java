package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setup() {
        product = new Product(
                null, 10, 5, ProductType.NORMAL, "Test Product",
                LocalDate.now().plusDays(20), LocalDate.now().minusDays(5), LocalDate.now().plusDays(10)
        );
    }

    @Test
    void notifyDelay_ShouldUpdateLeadTime_AndSendNotification() {
        productService.notifyDelay(3, product);

        assertThat(product.getLeadTime()).isEqualTo(3);
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(3, "Test Product");
    }

    @Test
    void handleSeasonalProduct_ShouldSetUnavailable_WhenSeasonOver() {
        product.setSeasonEndDate(LocalDate.now().minusDays(1));
        product.setAvailable(5);

        productService.handleSeasonalProduct(product);

        assertThat(product.getAvailable()).isEqualTo(0);
        verify(notificationService, times(1)).sendOutOfStockNotification(product.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void handleSeasonalProduct_ShouldNotifyOutOfStock_WhenSeasonNotStarted() {
        product.setSeasonStartDate(LocalDate.now().plusDays(10));

        productService.handleSeasonalProduct(product);

        verify(notificationService, times(1)).sendOutOfStockNotification(product.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void handleSeasonalProduct_ShouldCallNotifyDelay_WhenInSeason() {
        product.setLeadTime(5);
        product.setAvailable(0);
        product.setSeasonStartDate(LocalDate.now().minusDays(5));
        product.setSeasonEndDate(LocalDate.now().plusDays(10));

        productService.handleSeasonalProduct(product);

        verify(notificationService, times(1)).sendDelayNotification(5, product.getName());
    }

    @Test
    void handleExpiredProduct_ShouldReduceStock_WhenNotExpired() {
        product.setExpiryDate(LocalDate.now().plusDays(10));
        product.setAvailable(3);

        productService.handleExpiredProduct(product);

        assertThat(product.getAvailable()).isEqualTo(2);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void handleExpiredProduct_ShouldSendExpirationNotification_WhenExpired() {
        product.setExpiryDate(LocalDate.now().minusDays(1));
        product.setAvailable(3);

        productService.handleExpiredProduct(product);

        assertThat(product.getAvailable()).isEqualTo(0);
        verify(notificationService, times(1)).sendExpirationNotification(product.getName(), product.getExpiryDate());
        verify(productRepository, times(1)).save(product);
    }
}
