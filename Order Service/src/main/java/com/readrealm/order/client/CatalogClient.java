package com.readrealm.order.client;

import com.readrealm.order.model.backend.catalog.BookResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Collection;
import java.util.List;

public interface CatalogClient {

        @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "getBooksByISBNsFallback")
        @Retry(name= "catalogRetry", fallbackMethod = "getBooksByISBNsFallback")
        @RateLimiter(name = "catalogRateLimiter", fallbackMethod = "getBooksByISBNsFallback")
        @GetExchange("/v1/books")
        List<BookResponse> getBooksByISBNs(@RequestParam(value = "isbn") Collection<String> isbns);

        default List<BookResponse> getBooksByISBNsFallback(Collection<String> isbns, Exception e) {
               throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog service is unavailable");
        }
}