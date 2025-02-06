package com.readrealm.catalog.dto.author;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record AuthorResponse(
        long id,
        String firstName,
        String lastName,
        List<BookInfo> books
) implements Serializable {

    public record BookInfo(
            String isbn,
            String title,
            BigDecimal price
    ) implements Serializable {}
}
