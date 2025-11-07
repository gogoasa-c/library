package com.github.gogoasac.infra.input.menu;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MenuHandler behaviour")
class MenuHandlerTest {

    @Nested
    @DisplayName("displayMenu")
    class DisplayMenuTests {
        private ByteArrayOutputStream outputStream;
        private PrintStream printStream;
        private MenuHandler handler;

        @BeforeEach
        void setUp() {
            outputStream = new ByteArrayOutputStream();
            printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

            final List<MenuItem> items = Arrays.asList(
                new MenuItem("First item", () -> {}),
                new MenuItem("Second item", () -> {})
            );

            final InputStream emptyInput = new ByteArrayInputStream(new byte[0]);
            handler = new MenuHandler("Test Menu", printStream, emptyInput) { };
            handler.setMenuItemList(items);
        }

        @Test
        @DisplayName("should write menu header, items and Back entry to writer")
        void shouldWriteMenuHeaderItemsAndBackEntry() {
            handler.displayMenu();
            final String written = outputStream.toString(StandardCharsets.UTF_8);

            assertTrue(written.contains("--- Test Menu ---"), "Menu header expected");
            assertTrue(written.contains("0) First item"), "First item label expected");
            assertTrue(written.contains("1) Second item"), "Second item label expected");
            assertTrue(written.contains("9) Back"), "Back entry expected");
        }
    }

    @Nested
    @DisplayName("pickOption")
    class PickOptionTests {
        private ByteArrayOutputStream outputStream;
        private PrintStream printStream;
        private AtomicBoolean firstActionCalled;
        private AtomicBoolean secondActionCalled;
        private MenuHandler handler;

        @BeforeEach
        void setUp() {
            outputStream = new ByteArrayOutputStream();
            printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

            firstActionCalled = new AtomicBoolean(false);
            secondActionCalled = new AtomicBoolean(false);

            final List<MenuItem> items = Arrays.asList(
                new MenuItem("First item", () -> firstActionCalled.set(true)),
                new MenuItem("Second item", () -> secondActionCalled.set(true))
            );

            // choose the first menu item by providing "1" (user visible index 1 -> internal index 0)
            final InputStream input = new ByteArrayInputStream("1\n".getBytes(StandardCharsets.UTF_8));
            handler = new MenuHandler("Test Menu", printStream, input) { };
            handler.setMenuItemList(items);
        }

        @Test
        @DisplayName("should execute the chosen menu action for a valid numeric choice")
        void shouldExecuteChosenMenuActionForValidNumericChoice() {
            handler.pickOption();
            final String written = outputStream.toString(StandardCharsets.UTF_8);

            assertTrue(firstActionCalled.get(), "Expected first action to be executed");
            assertFalse(secondActionCalled.get(), "Second action should not have been executed");
            assertTrue(written.contains("Choose:"), "Prompt should have been written to the writer");
        }
    }

    @Nested
    @DisplayName("error handling")
    class ErrorHandlingTests {
        @Test
        @DisplayName("should throw NumberFormatException for non-numeric input")
        void shouldThrowWhenNonNumericInput() {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
            final List<MenuItem> items = List.of(new MenuItem("Item", () -> {}));
            final InputStream input = new ByteArrayInputStream("not-a-number\n".getBytes(StandardCharsets.UTF_8));

            final MenuHandler handler = new MenuHandler("Test Menu", printStream, input) { };
            handler.setMenuItemList(items);

            assertThrows(NumberFormatException.class, handler::pickOption);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException with clear message for out-of-range numeric input")
        void shouldThrowWhenChoiceOutOfRangeWithClearMessage() {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
            final List<MenuItem> items = List.of(new MenuItem("Item", () -> {}));
            final InputStream input = new ByteArrayInputStream("99\n".getBytes(StandardCharsets.UTF_8));

            final MenuHandler handler = new MenuHandler("Test Menu", printStream, input) { };
            handler.setMenuItemList(items);

            final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                handler::pickOption);

            assertEquals("Supplied menu item does not exist.", exception.getMessage());
        }
    }
}