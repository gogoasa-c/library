package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.domain.entity.Author;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

public class AuthorMenu extends MenuHandler {
    private static final String MENU_NAME = "Authors";

    private final AuthorManagementInput authorInput;

    public AuthorMenu(final PrintStream printStream,
                      final BufferedReader sharedReader,
                      final AuthorManagementInput authorManagementInput) {
        super(MENU_NAME, printStream, sharedReader);

        super.setMenuItemList(List.of(
            new MenuItem("Add author", this::addAuthor),
            new MenuItem("List all authors", this::listAllAuthors),
            new MenuItem("View author by id", this::viewAuthorById)
        ));

        this.authorInput = Objects.requireNonNull(authorManagementInput, "authorManagementInput");
    }

    private void addAuthor() {
        final String name = super.readLine("Author name: ");

        if (name.isEmpty()) {
            super.printLine("Name cannot be empty");
            return;
        }

        try {
            final Author created = authorInput.addAuthor(new AddAuthorCommand(name));
            super.printLine("Author created: " + created);
        } catch (Exception e) {
            super.printLine("Failed to create author: " + e.getMessage());
        }
    }

    public void listAllAuthors() {
        final java.util.List<Author> authors = authorInput.getAll();

        if (authors == null || authors.isEmpty()) {
            super.printLine("No authors found.");
            return;
        }

        super.printLine("Authors:");
        authors.stream()
            .map(a -> "  " + a)
            .forEach(super::printLine);
    }

    private void viewAuthorById() {
        final Long id = super.readLong("Author id: ");
        if (id == null) {
            return;
        }

        final Author author = authorInput.getById(id);
        if (author == null) {
            super.printLine("Author not found.");
            return;
        }

        super.printLine("Author: " + author);
    }
}
