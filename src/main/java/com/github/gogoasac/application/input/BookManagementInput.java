package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.domain.entity.Book;

public interface BookManagementInput {
    Book addBook(AddBookCommand addBookCommand);
}
