package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.domain.entity.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorMenu Tests")
class AuthorMenuTest {

    private ByteArrayOutputStream outputBuffer;
    private PrintStream printStream;

    @BeforeEach
    void setUpStreams() {
        outputBuffer = new ByteArrayOutputStream();
        printStream = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("Add and list flows")
    class AddAndList {

        @Test
        void shouldAddAuthorAndThenListIt() {
            // Input sequence: 1 (Add), "Test Author" (name), 2 (List), 9 (Back)
            final String inputLines = String.join("\n", "1", "Test Author", "2", "9") + "\n";
            final InputStream inputStream = new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8));

            final MutableAuthorInput authorInput = new MutableAuthorInput();
            final AuthorMenu menu = new AuthorMenu(printStream, inputStream, authorInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Author created"), "Should report creation");
            assertTrue(output.contains("Test Author"), "Should include author name in output");
            assertTrue(output.contains("Authors:"), "Should list authors");

            final List<Author> persisted = authorInput.getAll();
            assertEquals(1, persisted.size(), "One author should be persisted");
            assertEquals("Test Author", persisted.getFirst().name());
        }
    }

    @Nested
    @DisplayName("View by id flows")
    class ViewById {

        @Test
        void shouldViewAuthorByIdWhenExists() {
            final MutableAuthorInput authorInput = new MutableAuthorInput();
            final Author created = authorInput.addAuthor(new AddAuthorCommand("Existing Author"));

            // 3 -> View author by id, then provide the id, then 9 -> Back
            final String inputLines = String.join("\n", "3", String.valueOf(created.id()), "9") + "\n";
            final InputStream inputStream = new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8));
            final AuthorMenu menu = new AuthorMenu(printStream, inputStream, authorInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Author:"), "Should print author header");
            assertTrue(output.contains("Existing Author"), "Should include the author's name");
        }

        @Test
        void shouldReportInvalidNumberWhenNonNumericIdProvided() {
            // 3 -> View author by id, provide invalid id 'abc', then 9 -> Back
            final String inputLines = String.join("\n", "3", "abc", "9") + "\n";
            final InputStream inputStream = new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8));
            final MutableAuthorInput authorInput = new MutableAuthorInput();
            final AuthorMenu menu = new AuthorMenu(printStream, inputStream, authorInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Invalid number: 'abc'") || output.contains("Invalid number"), "Should report invalid number parsing");
        }
    }

    @Nested
    @DisplayName("Validation and error handling")
    class ValidationAndErrors {

        @Test
        void shouldRejectEmptyNameWhenAddingAuthor() {
            // 1 -> Add author, then empty name, then 9 -> Back
            final String inputLines = String.join("\n", "1", "", "9") + "\n";
            final InputStream inputStream = new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8));
            final MutableAuthorInput authorInput = new MutableAuthorInput();
            final AuthorMenu menu = new AuthorMenu(printStream, inputStream, authorInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Name cannot be empty"), "Should reject empty author name");
            assertEquals(0, authorInput.getAll().size(), "No author should be persisted");
        }
    }

    // --- lightweight in-memory mock for AuthorManagementInput used by tests ---
    private static final class MutableAuthorInput implements AuthorManagementInput {
        private long nextId = 1L;
        private final List<Author> storage = new ArrayList<>();

        @Override
        public Author addAuthor(AddAuthorCommand cmd) {
            final Author created = new Author(nextId++, cmd.name());
            storage.add(created);
            return created;
        }

        @Override
        public Author getById(Long id) {
            return storage.stream().filter(a -> a.id().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Author> getAll() {
            return List.copyOf(storage);
        }
    }
}