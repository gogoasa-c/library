package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.domain.entity.Book;

import java.util.List;

/**
 * Input port defining the book management operations the application exposes.
 *
 * <p>Implementations should orchestrate validation and persistence and present
 * a small, testable API for creating, retrieving and performing actions on books.
 */
public interface BookManagementInput {
    Book addBook(AddBookCommand addBookCommand);
    List<Book> getAll();
    Book getById(Long id);
    void borrow(final Long bookId);
}
