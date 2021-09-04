package com.wiredbraincoffee.productapifunctional;

import com.wiredbraincoffee.productapifunctional.model.Product;
import com.wiredbraincoffee.productapifunctional.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static java.lang.System.out;

@Service
public class ProductApiFunctionalRunner implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductApiFunctionalRunner(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Override
    public void run(String... args) throws Exception {
        final Flux<Product> productFlux = Flux.just(new Product(null, "Big Latte", 2.99),
                                                    new Product(null, "Big Decaf", 2.49),
                                                    new Product(null, "Green Tea", 1.99))
                .flatMap(productRepository::save);
        productFlux.thenMany(productRepository.findAll())
                .subscribe(out::println);

    }
}
