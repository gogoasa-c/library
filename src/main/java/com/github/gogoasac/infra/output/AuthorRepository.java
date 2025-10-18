package com.github.gogoasac.infra.output;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.infra.output.base.AbstractFileRepository;

import java.util.List;
import java.util.Optional;

public class AuthorRepository extends AbstractFileRepository<Author> implements AuthorPersistence {
    private static final String FILE_PATH = "Authors.json";

    public AuthorRepository() {
        super(FILE_PATH, new TypeReference<>() {}, Author::id);
    }

    @Override
    public Author addAuthor(Author author) {
        return save(author);
    }

    @Override
    public Optional<Author> findById(Long id) {
        return super.findById(id);
    }

    @Override
    public List<Author> findAll() {
        return super.findAll();
    }

    @Override
    protected Author setId(Author author, Long id) {
        return new Author(id, author.name());
    }
}
