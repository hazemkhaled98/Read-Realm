package com.readrealm.order.model.backend.catalog;


import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record BookResponse(
        Long id,
        String isbn,
        String title,
        String description,
        BigDecimal price,
        List<AuthorInfo> authors,
        List<CategoryInfo> categories
) implements Serializable {

    public record AuthorInfo(
            Long id,
            String fullName
    ) implements Serializable {}

    public record CategoryInfo(
            Long id,
            String name
    ) implements Serializable {}
}
