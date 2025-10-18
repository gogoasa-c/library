package com.github.gogoasac.application.output;

import com.github.gogoasac.domain.entity.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorPersistence {
    Author addAuthor(final Author author);

    Optional<Author> findById(final Long id);

    List<Author> findAll();
}
