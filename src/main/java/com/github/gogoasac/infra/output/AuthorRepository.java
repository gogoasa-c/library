package com.github.gogoasac.infra.output;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.infra.output.base.AbstractFileRepository;

import java.util.List;
import java.util.Optional;

/**
 * File-backed repository for authors.
 *
 * <p>Uses {@link AbstractFileRepository} for JSON-backed persistence. The
 * repository is intentionally minimal: it provides simple CRUD-like methods
 * used by application services and tests. File IO and id generation are handled
 * by the base class.
 */
public class AuthorRepository extends AbstractFileRepository<Author> implements AuthorPersistence {
    private static final String FILE_PATH = "Authors.json";

    public AuthorRepository() {
        super(FILE_PATH, new TypeReference<>() {}, Author::id);
    }

    public AuthorRepository(final String filePath) {
        super(filePath, new TypeReference<>() {}, Author::id);
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
