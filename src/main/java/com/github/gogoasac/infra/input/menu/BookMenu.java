package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.domain.entity.Book;
import com.github.gogoasac.domain.entity.Collection;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CLI menu handling book-related interactions (add/list/view/borrow).
 *
 * <p>This {@link MenuHandler} coordinates interactions across the book, author
 * and collection input ports. It resolves author/collection names for listings
 * and delegates domain actions (e.g. borrowing) to the injected
 * {@link BookManagementInput}.
 *
 * <p>Notes:
 * - Uses shared IO (BufferedReader/PrintStream) to avoid competing readers.
 * - Displays borrowed date information when present for easy inspection.
 */
public final class BookMenu extends MenuHandler {
    private static final String MENU_NAME = "Books";

    // new: formatter for borrowed date
    private static final DateTimeFormatter BORROWED_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BookManagementInput bookInput;
    private final AuthorManagementInput authorInput;
    private final CollectionManagementInput collectionInput;

    public BookMenu(final PrintStream printStream,
                    final BufferedReader sharedReader,
                    final BookManagementInput bookManagementInput,
                    final AuthorManagementInput authorManagementInput,
                    final CollectionManagementInput collectionManagementInput) {
        super(MENU_NAME, printStream, sharedReader);

        super.setMenuItemList(List.of(
            new MenuItem("Add book", this::addBook),
            new MenuItem("List all books", this::listAllBooks),
            new MenuItem("View book by id", this::viewBookById),
            new MenuItem("Borrow a book", this::borrowBook)
        ));

        this.bookInput = Objects.requireNonNull(bookManagementInput, "bookManagementInput");
        this.authorInput = Objects.requireNonNull(authorManagementInput, "authorManagementInput");
        this.collectionInput = Objects.requireNonNull(collectionManagementInput, "collectionManagementInput");
    }

    private void addBook() {
        super.printLine("To add a book you need to provide title, author id and collection id.");
        listAuthorsInline();
        listAllCollections();

        final String title = super.readLine("Title: ").trim();
        if (title.isEmpty()) {
            super.printLine("Title cannot be empty");
            return;
        }
        final Long authorId = super.readLong("Author id: ");
        if (authorId == null) return;
        final Long collectionId = super.readLong("Collection id: ");
        if (collectionId == null) return;
        final Integer year = super.readInt("Publication year: ");
        if (year == null) return;

        try {
            final Book created = bookInput.addBook(new AddBookCommand(title, authorId, collectionId, year));
            super.printLine("Book created: " + created);
        } catch (Exception e) {
            super.printLine("Failed to create book: " + e.getMessage());
        }
    }

    private void listAuthorsInline() {
        final List<Author> authors = authorInput.getAll();
        if (authors == null || authors.isEmpty()) {
            super.printLine("No authors found.");
            return;
        }
        super.printLine("Authors:");
        authors.stream()
               .map(a -> "  " + a.id() + ") " + a)
               .forEach(super::printLine);
    }

    private void listAllCollections() {
        final List<Collection> collections = collectionInput.getAll();
        if (collections == null || collections.isEmpty()) {
            super.printLine("No collections found.");
            return;
        }
        super.printLine("Collections:");
        collections.stream()
                   .map(c -> "  " + c.id() + ") " + c)
                   .forEach(super::printLine);
    }

    private void listAllBooks() {
        final List<Book> books = bookInput.getAll();
        if (books == null || books.isEmpty()) {
            super.printLine("No books found.");
            return;
        }

        final Map<Long, String> authorsById = authorInput.getAll().stream()
                .collect(Collectors.toMap(Author::id, Author::toString));
        final Map<Long, String> collectionsById = collectionInput.getAll().stream()
                .collect(Collectors.toMap(Collection::id, Collection::toString));

        super.printLine("Books:");
        books.stream()
             .map(b -> {
                 final String authorStr = authorsById.getOrDefault(b.authorId(), "<unknown>");
                 final String collectionStr = collectionsById.getOrDefault(b.collectionId(), "<unknown>");
                 final String borrowedInfo = b.borrowedAt() == null
                         ? ""
                         : " | Borrowed: " + b.borrowedAt().format(BORROWED_FMT);
                 return String.format("  %d) %s | Author: %s | Collection: %s | Year: %s%s",
                         b.id(), b.title(), authorStr, collectionStr, b.publicationYear(), borrowedInfo);
             })
             .forEach(super::printLine);
    }

    private void viewBookById() {
        final Long id = super.readLong("Book id: ");
        if (id == null) return;
        final Book b = bookInput.getById(id);
        if (b == null) {
            super.printLine("Book not found.");
            return;
        }

        final String authorStr = Optional.ofNullable(authorInput.getById(b.authorId())).map(Author::toString).orElse("<unknown>");
        final String collectionStr = Optional.ofNullable(collectionInput.getById(b.collectionId())).map(Collection::toString).orElse("<unknown>");

        final StringBuilder details = new StringBuilder();
        details.append(String.format("Book: id=%d, title=%s, author=%s, collection=%s, year=%s",
                b.id(), b.title(), authorStr, collectionStr, b.publicationYear()));

        if (b.borrowedAt() != null) {
            details.append(String.format(" | BorrowedAt: %s | Borrowed: %s",
                    b.borrowedAt().format(BORROWED_FMT), b.isBorrowed()));
        }

        super.printLine(details.toString());
    }

    private void borrowBook() {
        final Long id = super.readLong("Book id to borrow: ");
        if (id == null) return;

        this.bookInput.borrow(id);
    }
}
