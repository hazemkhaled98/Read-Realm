package com.readrealm.catalog.mapper;

import com.readrealm.catalog.dto.author.AuthorResponse;
import com.readrealm.catalog.entity.Author;
import com.readrealm.catalog.repository.projection.AuthorDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorResponse toAuthorResponse(AuthorDetails authorDetails);

    AuthorResponse toAuthorResponse(Author author);
}
