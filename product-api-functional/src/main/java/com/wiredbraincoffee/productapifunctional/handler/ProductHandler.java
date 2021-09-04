package com.wiredbraincoffee.productapifunctional.handler;

import com.wiredbraincoffee.productapifunctional.model.Product;
import com.wiredbraincoffee.productapifunctional.model.ProductEvent;
import com.wiredbraincoffee.productapifunctional.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ProductHandler {

    private final ProductRepository productRepository;

    public ProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<ServerResponse> getAllProducts(ServerRequest serverRequest) {
        final Flux<Product> products = productRepository.findAll();
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest serverRequest) {
        final String id = serverRequest.pathVariable("id");
        final Mono<Product> productMono = productRepository.findById(id);
        return productMono.flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(product)))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> saveProduct(ServerRequest serverRequest) {
        final Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        return productMono.flatMap(product -> ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(productRepository.save(product), Product.class));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest serverRequest) {
        final String id = serverRequest.pathVariable("id");
        Mono<Product> existingProductMono = productRepository.findById(id);
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        return productMono.zipWith(existingProductMono, (product, existingProduct) -> new Product(existingProduct.getId(), product.getName(), product.getPrice()))
                .flatMap(product -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(productRepository.save(product), Product.class)
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest serverRequest) {
        final String id = serverRequest.pathVariable("id");
        final Mono<Product> productMono = productRepository.findById(id);
        return productMono.flatMap(existinProduct -> ServerResponse.ok()
                        .build(productRepository.delete(existinProduct)))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> deleteAllProducts(ServerRequest serverRequest) {
        return ServerResponse.ok().build(productRepository.deleteAll());
    }

    public Mono<ServerResponse> getProductEvents(ServerRequest serverRequest) {
        Flux<ProductEvent> eventFlux = Flux.interval(Duration.ofSeconds(1))
                .map(val -> new ProductEvent(val, "Product Event"));
        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventFlux, ProductEvent.class);
    }
}
