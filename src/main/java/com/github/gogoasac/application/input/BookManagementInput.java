package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.domain.entity.Book;

import java.util.List;

public interface BookManagementInput {
    Book addBook(AddBookCommand addBookCommand);
    List<Book> getAll();
    Book getById(Long id);
}
