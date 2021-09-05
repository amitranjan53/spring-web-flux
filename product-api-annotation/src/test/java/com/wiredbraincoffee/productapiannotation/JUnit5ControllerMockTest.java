package com.wiredbraincoffee.productapiannotation;

import com.wiredbraincoffee.productapiannotation.controller.ProductController;
import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.model.ProductEvent;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class JUnit5ControllerMockTest {

    private WebTestClient webTestClient;
    private List<Product> expectedList;

    @MockBean
    private ProductRepository productRepository;

    @BeforeEach
    void beforeEach() {
        this.webTestClient = WebTestClient.bindToController(new ProductController(productRepository))
                .configureClient()
                .baseUrl("/products")
                .build();
        this.expectedList = Arrays.asList(new Product("1", "Big Latter", 2.99));

    }

    @Test
    void testGetAllProducts() {
        Mockito.when(productRepository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));
        webTestClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }

    @Test
    void testProductInvalidIdNotFound() {
        String id = "aaa";
        when(productRepository.findById(id)).thenReturn(Mono.empty());
        webTestClient
                .get()
                .uri("/aaa")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testProductIdFound() {
        final Product expectedProduct = expectedList.get(0);
        when(productRepository.findById(expectedProduct.getId())).thenReturn(Mono.just(expectedProduct));
        webTestClient
                .get()
                .uri("/{id}", expectedProduct.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }

    @Test
    void testProductEvents() {
        final ProductEvent productEvent = new ProductEvent(0L, "Product Event");
        final FluxExchangeResult<ProductEvent> result = webTestClient
                .get()
                .uri("/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(ProductEvent.class);

        StepVerifier.create(result.getResponseBody())
                .expectNext(productEvent)
                .expectNextCount(2)
                .consumeNextWith(event -> assertEquals(Long.valueOf(3), event.getEventId()))
                .thenCancel()
                .verify();
    }

}
