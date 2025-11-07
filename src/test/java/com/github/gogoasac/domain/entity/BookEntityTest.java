package com.github.gogoasac.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Book entity behaviour")
class BookEntityTest {

    @Test
    void shouldSetBorrowedAtAndFlagWhenBorrowed() {
        final Book original = new Book(1L, "Some Title", 1L, 1L, 2020);
        final Book borrowed = original.borrow();

        assertTrue(borrowed.isBorrowed());
        assertNotNull(borrowed.borrowedAt());
        assertEquals(original.id(), borrowed.id());
        assertEquals(original.title(), borrowed.title());
    }

    @Test
    void shouldThrowWhenBorrowingAlreadyBorrowedBook() {
        final Book original = new Book(1L, "Some Title", 1L, 1L, 2020);
        final Book borrowed = original.borrow();

        IllegalStateException ex = assertThrows(IllegalStateException.class, borrowed::borrow);
        assertEquals("Book is already borrowed.", ex.getMessage());
    }
}

