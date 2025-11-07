package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.application.output.BookPersistence;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Book;

import java.util.List;

public final class BookManagementService implements BookManagementInput {
    private final BookPersistence bookPersistence;
    private final AuthorPersistence authorPersistence;
    private final CollectionPersistence collectionPersistence;

    public BookManagementService(BookPersistence bookPersistence, AuthorPersistence authorPersistence, CollectionPersistence collectionPersistence) {
        this.bookPersistence = bookPersistence;
        this.authorPersistence = authorPersistence;
        this.collectionPersistence = collectionPersistence;
    }

    @Override
    public Book addBook(AddBookCommand addBookCommand) {
        if (authorPersistence.findById(addBookCommand.authorId()).isEmpty()) {
            throw new IllegalArgumentException("Author with ID " + addBookCommand.authorId() + " does not exist.");
        }

        if (collectionPersistence.findById(addBookCommand.collectionId()).isEmpty()) {
            throw new IllegalArgumentException("Collection with ID " + addBookCommand.collectionId() + " does not exist.");
        }

        Book book = new Book(
            null,
            addBookCommand.title(),
            addBookCommand.authorId(),
            addBookCommand.collectionId(),
            addBookCommand.publicationYear(),
            null,
            false
        );

        return bookPersistence.addBook(book);
    }

    @Override
    public List<Book> getAll() {
        return bookPersistence.findAll();
    }

    @Override
    public Book getById(Long id) {
        return bookPersistence.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Book with ID " + id + " does not exist."));
    }

    public void borrow(final Long bookId) {
        final Book book = this.bookPersistence.findById(bookId).orElseThrow();

        this.bookPersistence.updateBook(book.borrow()).orElseThrow();
    }
}
