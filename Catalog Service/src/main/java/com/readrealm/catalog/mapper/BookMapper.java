package com.readrealm.catalog.mapper;

import com.readrealm.catalog.dto.book.BookResponse;
import com.readrealm.catalog.entity.Book;
import com.readrealm.catalog.repository.projection.BookDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponse toBookResponse(BookDetails bookDetails);

    BookResponse toBookResponse(Book book);
}
