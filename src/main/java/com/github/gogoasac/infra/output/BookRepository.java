package com.github.gogoasac.infra.output;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.gogoasac.application.output.BookPersistence;
import com.github.gogoasac.domain.entity.Book;
import com.github.gogoasac.infra.output.base.AbstractFileRepository;

import java.util.List;
import java.util.Optional;

public class BookRepository extends AbstractFileRepository<Book> implements BookPersistence {
    private static final String FILE_PATH = "Books.json";

    public BookRepository() {
        super(FILE_PATH, new TypeReference<>() {}, Book::id);
    }

    @Override
    public Book addBook(Book book) {
        return super.save(book);
    }

    @Override
    public Optional<Book> findById(Long id) {
        return super.findById(id);
    }

    @Override
    public List<Book> findAll() {
        return super.findAll();
    }

    @Override
    protected Book setId(Book book, Long id) {
        return new Book(
            id,
            book.title(),
            book.authorId(),
            book.collectionId(),
            book.publicationYear()
        );
    }
}
