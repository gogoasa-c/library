package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.application.output.BookPersistence;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Book;

public class BookManagementService implements BookManagementInput {
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

        return null;
    }
}
