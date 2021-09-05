package com.wiredbraincoffee.productapifunctional.config;

import com.wiredbraincoffee.productapifunctional.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class ApiFunctionalConfig {

    @Bean
    RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
        //simple example
//        return RouterFunctions.route()
//                .GET("/products/events", RequestPredicates.accept(MediaType.TEXT_EVENT_STREAM), productHandler::getProductEvents)
//                .GET("/products/{id}", RequestPredicates.accept(MediaType.APPLICATION_JSON), productHandler::getProduct)
//                .GET("/products", RequestPredicates.accept(MediaType.APPLICATION_JSON), productHandler::getAllProducts)
//                .PUT("/products/{id}", RequestPredicates.accept(MediaType.APPLICATION_JSON), productHandler::updateProduct)
//                .POST("/products", RequestPredicates.accept(MediaType.APPLICATION_JSON), productHandler::saveProduct)
//                .DELETE("/products/{id}", RequestPredicates.accept(MediaType.APPLICATION_JSON), productHandler::deleteProduct)
//                .DELETE("/products", RequestPredicates.accept(MediaType.APPLICATION_JSON), productHandler::deleteAllProducts)
//                .build();
        //nested example
        return RouterFunctions.route()
                .path("/products",
                      builder -> builder.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON)
                                                      .or(contentType(MediaType.APPLICATION_JSON))
                                                      .or(RequestPredicates.accept(MediaType.TEXT_EVENT_STREAM)),
                                              nestestBuilder -> nestestBuilder.GET("/events", productHandler::getProductEvents)
                                                      .GET("/{id}", productHandler::getProduct)
                                                      .GET(productHandler::getAllProducts)
                                                      .PUT("/{id}", productHandler::updateProduct)
                                                      .POST(productHandler::saveProduct))
                              .DELETE("/{id}", productHandler::deleteProduct)
                              .DELETE(productHandler::deleteAllProducts))
                .build();
    }

}
