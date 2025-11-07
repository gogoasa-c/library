package com.github.gogoasac.application.output;

import com.github.gogoasac.domain.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookPersistence {
    Book addBook(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    Optional<Book> updateBook(final Book book);
}
