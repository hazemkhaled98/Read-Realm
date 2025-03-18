package com.readrealm.order.client;

import com.readrealm.order.model.backend.catalog.BookResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Collection;
import java.util.List;

public interface CatalogClient {

        @GetExchange("/v1/books")
        List<BookResponse> getBooksByISBNs(@RequestParam(value = "isbn") Collection<String> isbns);
}