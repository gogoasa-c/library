package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.domain.entity.Author;

import java.util.List;

public class AuthorManagementService implements AuthorManagementInput {
    private final AuthorPersistence authorPersistence;

    public AuthorManagementService(AuthorPersistence authorPersistence) {
        this.authorPersistence = authorPersistence;
    }

    @Override
    public Author addAuthor(AddAuthorCommand addAuthorCommand) {
        Author author = new Author(null, addAuthorCommand.name());

        return this.authorPersistence.addAuthor(author);
    }

    @Override
    public List<Author> getAll() {
        return this.authorPersistence.findAll();
    }

    @Override
    public Author getById(Long id) {
        return this.authorPersistence.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Author with ID " + id + " does not exist."));
    }
}