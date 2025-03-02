package com.readrealm.catalog.service;

import com.readrealm.catalog.dto.author.AuthorRequest;
import com.readrealm.catalog.dto.author.AuthorResponse;
import com.readrealm.catalog.dto.author.UpdateAuthorRequest;
import com.readrealm.catalog.entity.Author;
import com.readrealm.catalog.mapper.AuthorMapper;
import com.readrealm.catalog.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "authors", cacheManager = "cacheManager")
    public List<AuthorResponse> findAllAuthors() {

        log.info("Finding All Authors");
        return authorRepository.findAllAuthorDetails()
                .stream()
                .map(authorMapper::toAuthorResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "authorById", key = "#id", cacheManager = "cacheManager")
    public AuthorResponse findAuthorById(Long id) {
        log.info("Finding Author by ID {}", id);
        return authorRepository.findAuthorDetailsById(id)
                .map(authorMapper::toAuthorResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Author with id %s not found", id)));
    }

    @Transactional
    @CachePut(cacheNames = "authorById", key = "#createdAuthor.id()", cacheManager = "cacheManager")
    @CacheEvict(cacheNames = "authors", allEntries = true, cacheManager = "cacheManager")
    public AuthorResponse addAuthor(AuthorRequest request) {

        log.info("Adding Author {}", request);
        Author createdAuthor = Author.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .build();

        createdAuthor = authorRepository.save(createdAuthor);

        return authorMapper.toAuthorResponse(createdAuthor);
    }

    @Transactional
    @CachePut(cacheNames = "authorById", key = "#updatedAuthor.id()", cacheManager = "cacheManager")
    @CacheEvict(cacheNames = "authors", allEntries = true, cacheManager = "cacheManager")
    public AuthorResponse updateAuthor(UpdateAuthorRequest request) {
        log.info("Updating Author {}", request);

        long id = Long.parseLong(request.id());
        Optional<Author> optionalAuthor = authorRepository.findById(id);

        Author updatedAuthor = optionalAuthor
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Author with id %s not found", request.id())));

        updatedAuthor.setFirstName(request.details().firstName());
        updatedAuthor.setLastName(request.details().lastName());

        updatedAuthor = authorRepository.save(updatedAuthor);

        return authorMapper.toAuthorResponse(updatedAuthor);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "authorById", key = "#isbn", cacheManager = "cacheManager"),
            @CacheEvict(cacheNames = "authors", allEntries = true, cacheManager = "cacheManager")
    })
    public void deleteAuthor(long id) {
        log.info("Deleting Author with id {}", id);
        authorRepository.deleteById(id);
    }
}
