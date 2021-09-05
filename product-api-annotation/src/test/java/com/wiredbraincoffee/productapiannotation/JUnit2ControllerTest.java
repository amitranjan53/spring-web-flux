package com.wiredbraincoffee.productapiannotation;

import com.wiredbraincoffee.productapiannotation.controller.ProductController;
import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.model.ProductEvent;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class JUnit2ControllerTest {

    private WebTestClient webTestClient;
    private List<Product> expectedList;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void beforeEach() {
        this.webTestClient = WebTestClient.bindToController(new ProductController(productRepository))
                .configureClient()
                .baseUrl("/products")
                .build();
        this.expectedList = productRepository.findAll().collectList().block();

    }

    @Test
    void testGetAllProducts() {
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
