package webclient;

import model.Product;
import model.ProductEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class WebClientApi {

    private final WebClient webClient;

    public WebClientApi() {
//        webClient = WebClient.create("http://localhost:8080/products");
        webClient = WebClient.builder().baseUrl("http://localhost:8080/products").build();
    }

    public static void main(String[] args) {
        WebClientApi webClientApi = new WebClientApi();
        webClientApi.postNewProduct()
                .thenMany(webClientApi.getAllProducts())
                .take(1)
                .flatMap(p -> webClientApi.updateProduct(p.getId(), "White Tea", 0.99))
                .flatMap(p -> webClientApi.deleteProduct(p.getId()))
                .thenMany(webClientApi.getAllProducts())
                .subscribeOn(Schedulers.newSingle("new Thread"))
                .thenMany(webClientApi.getAllEvents())
                .subscribe(System.out::println);

//        try {
//            Thread.sleep(5000);
//        } catch (Exception e) {
//
//        }
    }

    private Mono<ResponseEntity<Product>> postNewProduct() {
        return webClient
                .post()
                .body(Mono.just(new Product(null, "Jasmine Team", 1.99)), Product.class)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(Product.class))
                .doOnSuccess(o -> System.out.println("******POST" + o));
    }

    private Flux<Product> getAllProducts() {
        return webClient
                .get()
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(o -> System.out.println("*****GET" + o));
    }

    private Mono<Product> updateProduct(String id, String name, double price) {
        return webClient
                .put()
                .uri("/{id}", id)
                .body(Mono.just(new Product(null, name, price)), Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(o -> System.out.println("******UPDATE" + o));
    }

    private Mono<Void> deleteProduct(String id) {
        return webClient
                .delete()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(o -> System.out.println("******DELETE" + o));
    }

    private Flux<ProductEvent> getAllEvents() {
        return webClient
                .get()
                .uri("/events")
                .retrieve()
                .bodyToFlux(ProductEvent.class);
    }

}
