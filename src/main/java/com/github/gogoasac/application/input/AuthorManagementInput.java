package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.domain.entity.Author;

import java.util.List;

public interface AuthorManagementInput {
    Author addAuthor(AddAuthorCommand addAuthorCommand);
    List<Author> getAll();
    Author getById(Long id);
}
