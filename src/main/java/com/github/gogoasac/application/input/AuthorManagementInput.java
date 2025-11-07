package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.domain.entity.Author;

import java.util.List;

/**
 * Input port for author-related application operations.
 *
 * <p>Defines the contract used by UI and other adapters to create and retrieve authors.
 */
public interface AuthorManagementInput {
    Author addAuthor(AddAuthorCommand addAuthorCommand);
    List<Author> getAll();
    Author getById(Long id);
}
