package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.domain.entity.Author;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;

public class AuthorMenu extends MenuHandler {
    private final AuthorManagementInput authorInput;

    public AuthorMenu(final String menuName,
                      final PrintWriter writer,
                      final BufferedReader reader,
                      final AuthorManagementInput authorManagementInput) {
        super(menuName, writer, reader);

        super.setMenuItemList(List.of(
            new MenuItem("Add author", this::addAuthor),
            new MenuItem("List all authors", this::listAllAuthors),
            new MenuItem("View author by id", this::viewAuthorById)));

        this.authorInput = authorManagementInput;
    }



    private void addAuthor() {
        final String name = super.readLine("Author name: ").trim();

        if (name.isEmpty()) {
            super.printLine("Name cannot be empty");
            return;
        }

        try {
            final Author created = authorInput.addAuthor(new AddAuthorCommand(name));
            super.printLine("Author created: " + created.toString());
        } catch (Exception e) {
            super.printLine("Failed to create author: " + e.getMessage());
        }
    }

    private void listAllAuthors() {
        final List<Author> authors = authorInput.getAll();

        if (authors.isEmpty()) {
            super.printLine("No authors found.");
            return;
        }

        super.printLine("Authors:");

        authors.stream()
            .map(a -> "  " + a.toString())
            .forEach(super::printLine);
    }

    private void viewAuthorById() {
        Long id = super.readLong("Author id: ");

        if (id == null) return;

        try {
            Author a = authorInput.getById(id);
            super.printLine("Author: " + a.toString());
        } catch (Exception e) {
            super.printLine("Error: " + e.getMessage());
        }
    }

}
