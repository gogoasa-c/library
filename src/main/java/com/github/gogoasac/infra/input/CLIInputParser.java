package com.github.gogoasac.infra.input;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.application.dto.CollectionReport;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.application.input.ReportingInput;
import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.domain.entity.Book;
import com.github.gogoasac.domain.entity.Collection;
import com.github.gogoasac.infra.input.menu.AuthorMenu;
import com.github.gogoasac.infra.input.reporting.ReportViewer;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Simple terminal UI for the application. Keeps logic thin and delegates to application services.
 * The parser is IO-agnostic: input and output are injected so it can be tested easily.
 * Provides both a full constructor (recommended) and a no-arg convenience constructor
 * that delegates to the existing DependencyOrchestrator and System streams.
 */
public final class CLIInputParser {
    private final AuthorManagementInput authorInput;
    private final BookManagementInput bookInput;
    private final CollectionManagementInput collectionInput;
    private final ReportingInput reportingInput;

    private final BufferedReader reader;
    private final PrintWriter writer;

    private final Supplier<String> lineReader;

    private final ReportViewer reportViewer;
    private final AuthorMenu authorMenu;

//    public CLIInputParser() {
//        this(
//            DependencyOrchestrator.INSTANCE.authorManagementInput,
//            DependencyOrchestrator.INSTANCE.bookManagementInput,
//            DependencyOrchestrator.INSTANCE.collectionManagementInput,
//            DependencyOrchestrator.INSTANCE.reportingInput,
//            System.in,
//            System.out,
//            DependencyOrchestrator.INSTANCE.reportViewer,
//            DependencyOrchestrator.INSTANCE
//        );
//    }

    public CLIInputParser(
        AuthorManagementInput authorInput,
        BookManagementInput bookInput,
        CollectionManagementInput collectionInput,
        ReportingInput reportingInput,
        InputStream in,
        PrintStream out,
        ReportViewer reportViewer,
        AuthorMenu authorMenu
    ) {

        this.authorInput = authorInput;
        this.bookInput = bookInput;
        this.collectionInput = collectionInput;
        this.reportingInput = reportingInput;

        this.reader = new BufferedReader(new InputStreamReader(in));
        this.writer = new PrintWriter(out, true);

        this.lineReader = () -> {
            try {
                String line = reader.readLine();
                return line == null ? "" : line;
            } catch (IOException e) {
                return "";
            }
        };

        this.reportViewer = reportViewer;

        this.authorMenu = authorMenu;
    }

    public void run() {
        println("Welcome to the Library TUI");
        while (true) {
            printMainMenu();
            String option = readLine("Select an option: ").trim();
            switch (option) {
                case "1" -> handleAuthorsMenu();
                case "2" -> handleCollectionsMenu();
                case "3" -> handleBooksMenu();
                case "4" -> handleReports();
                case "0" -> {
                    println("Exiting. Goodbye!");
                    return;
                }
                default -> println("Unknown option. Please choose a valid menu number.");
            }
            println("");
        }
    }

    private void printMainMenu() {
        println("=== Main Menu ===");
        println("1) Authors");
        println("2) Collections");
        println("3) Books");
        println("4) Generate Collection Reports (writes report_YYYY-MM-DD.txt)");
        println("0) Exit");
    }

    private void handleAuthorsMenu() {
        this.authorMenu.run();
//        while (true) {
//            println("\n--- Authors ---");
//            println("1) Add author");
//            println("2) List all authors");
//            println("3) View author by id");
//            println("9) Back");
//            String opt = readLine("Choose: ").trim();
//            switch (opt) {
//                case "1" -> addAuthor();
//                case "2" -> listAuthors();
//                case "3" -> viewAuthorById();
//                case "9" -> {
//                    return;
//                }
//                default -> println("Invalid option");
//            }
//        }
    }

    private void handleCollectionsMenu() {
        while (true) {
            println("\n--- Collections ---");
            println("1) Add collection");
            println("2) List all collections");
            println("3) View collection by id");
            println("9) Back");
            String opt = readLine("Choose: ").trim();
            switch (opt) {
                case "1" -> addCollection();
                case "2" -> listCollections();
                case "3" -> viewCollectionById();
                case "9" -> {
                    return;
                }
                default -> println("Invalid option");
            }
        }
    }

    private void handleBooksMenu() {
        while (true) {
            println("\n--- Books ---");
            println("1) Add book");
            println("2) List all books");
            println("3) View book by id");
            println("9) Back");
            String opt = readLine("Choose: ").trim();
            switch (opt) {
                case "1" -> addBook();
                case "2" -> listBooks();
                case "3" -> viewBookById();
                case "9" -> {
                    return;
                }
                default -> println("Invalid option");
            }
        }
    }

    private void handleReports() {
        println("Generating collection reports...");
        try {
            final List<CollectionReport> reports = reportingInput.generateCollectionReports();
            println("Report generated. A file named report_YYYY-MM-DD.txt was written to the working directory.");
            println("Collections found: " + reports.size());

            // Offer GUI view if we actually have collection reports
            if (!reports.isEmpty()) {
                final String open = readLine("Open report in GUI? (y/N): ").trim();
                if ("y".equalsIgnoreCase(open)) {
                    try {
                        this.reportViewer.showReports(reports);
                        println("Report viewer opened.");
                    } catch (ClassCastException ex) {
                        println("Unable to open GUI viewer: report format not recognized.");
                    } catch (Exception ex) {
                        println("Failed to open report viewer: " + ex.getMessage());
                    }
                } else {
                    println("Skipping GUI view.");
                }
            }
        } catch (Exception e) {
            println("Failed to generate reports: " + e.getMessage());
        }
    }

    private void addAuthor() {
        String name = readLine("Author name: ").trim();
        if (name.isEmpty()) {
            println("Name cannot be empty");
            return;
        }
        try {
            Author created = authorInput.addAuthor(new AddAuthorCommand(name));
            println("Author created: " + created.toString());
        } catch (Exception e) {
            println("Failed to create author: " + e.getMessage());
        }
    }

    private void listAuthors() {
        List<Author> authors = authorInput.getAll();
        if (authors.isEmpty()) {
            println("No authors found.");
            return;
        }
        println("Authors:");
        authors.stream()
               .map(a -> "  " + a.toString())
               .forEach(this::println);
    }

    private void viewAuthorById() {
        Long id = readLong("Author id: ");
        if (id == null) return;
        try {
            Author a = authorInput.getById(id);
            println("Author: " + a.toString());
        } catch (Exception e) {
            println("Error: " + e.getMessage());
        }
    }

    private void addCollection() {
        String name = readLine("Collection name: ").trim();
        if (name.isEmpty()) {
            println("Name cannot be empty");
            return;
        }
        try {
            Collection created = collectionInput.addCollection(new AddCollectionCommand(name));
            println("Collection created: " + created.toString());
        } catch (Exception e) {
            println("Failed to create collection: " + e.getMessage());
        }
    }

    private void listCollections() {
        List<Collection> collections = collectionInput.getAll();
        if (collections.isEmpty()) {
            println("No collections found.");
            return;
        }
        println("Collections:");
        collections.stream()
                   .map(c -> "  " + c.toString())
                   .forEach(this::println);
    }

    private void viewCollectionById() {
        Long id = readLong("Collection id: ");
        if (id == null) return;
        try {
            Collection c = collectionInput.getById(id);
            println("Collection: " + c.toString());
        } catch (Exception e) {
            println("Error: " + e.getMessage());
        }
    }

    private void addBook() {
        println("To add a book you need to provide title, author id and collection id.");
        listAuthors();
        listCollections();

        String title = readLine("Title: ").trim();
        if (title.isEmpty()) {
            println("Title cannot be empty");
            return;
        }
        Long authorId = readLong("Author id: ");
        if (authorId == null) return;
        Long collectionId = readLong("Collection id: ");
        if (collectionId == null) return;
        Integer year = readInt("Publication year: ");
        if (year == null) return;

        try {
            Book created = bookInput.addBook(new AddBookCommand(title, authorId, collectionId, year));
            println("Book created: " + created.toString());
        } catch (Exception e) {
            println("Failed to create book: " + e.getMessage());
        }
    }

    private void listBooks() {
        List<Book> books = bookInput.getAll();
        if (books.isEmpty()) {
            println("No books found.");
            return;
        }
        Map<Long, String> authorsById = authorInput.getAll().stream()
                .collect(Collectors.toMap(Author::id, Author::toString));
        Map<Long, String> collectionsById = collectionInput.getAll().stream()
                .collect(Collectors.toMap(Collection::id, Collection::toString));

        println("Books:");
        books.stream()
             .map(b -> {
                 String authorStr = authorsById.getOrDefault(b.authorId(), "<unknown>");
                 String collectionStr = collectionsById.getOrDefault(b.collectionId(), "<unknown>");
                 return String.format("  %d) %s | Author: %s | Collection: %s | Year: %s",
                         b.id(), b.title(), authorStr, collectionStr, b.publicationYear());
             })
             .forEach(this::println);
    }

    private void viewBookById() {
        Long id = readLong("Book id: ");
        if (id == null) return;
        try {
            Book b = bookInput.getById(id);
            String authorStr = Optional.ofNullable(authorInput.getById(b.authorId()))
                    .map(Author::toString)
                    .orElse("<unknown>");
            String collectionStr = Optional.ofNullable(collectionInput.getById(b.collectionId()))
                    .map(Collection::toString)
                    .orElse("<unknown>");
            println(String.format("Book: id=%d, title=%s, author=%s, collection=%s, year=%s",
                    b.id(), b.title(), authorStr, collectionStr, b.publicationYear()));
        } catch (Exception e) {
            println("Error: " + e.getMessage());
        }
    }

    private String readLine(String prompt) {
        writer.print(prompt);
        writer.flush();
        String line = lineReader.get();
        return line == null ? "" : line;
    }

    private Long readLong(String prompt) {
        String s = readLine(prompt).trim();
        if (s.isEmpty()) {
            println("Cancelled");
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            println("Invalid number: '" + s + "'");
            return null;
        }
    }

    private Integer readInt(String prompt) {
        String s = readLine(prompt).trim();
        if (s.isEmpty()) {
            println("Cancelled");
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            println("Invalid number: '" + s + "'");
            return null;
        }
    }

    private void println(String s) {
        writer.println(s);
    }
}
