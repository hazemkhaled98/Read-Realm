package com.readrealm.order.client;

import com.readrealm.order.model.backend.catalog.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(name = "catalogClient", url = "http://localhost:8080/v1/books")
public interface CatalogClient {

        @GetMapping
        List<BookResponse> getBooksByISBNs(@RequestParam(value = "isbn") Collection<String> isbns);
}