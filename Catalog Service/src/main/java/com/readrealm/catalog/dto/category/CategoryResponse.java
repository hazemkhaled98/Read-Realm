package com.readrealm.catalog.dto.category;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record CategoryResponse(
        long id,
        String name,
        List<BookInfo> books
) implements Serializable {

    public record BookInfo(
            String isbn,
            String title,
            BigDecimal price
    ) implements Serializable {}
}
