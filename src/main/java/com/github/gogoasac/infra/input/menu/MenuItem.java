package com.github.gogoasac.infra.input.menu;

/**
 * Small immutable value describing a single menu entry.
 *
 * <p>A MenuItem pairs the text shown to the user with a zero-argument action
 * that will be executed when the item is chosen. It is intentionally tiny and
 * encourages use of method references or small lambdas for the action.
 */
public record MenuItem(String content, Runnable action) {

    public static MenuItem of(final String content, final Runnable action) {
        return new MenuItem(content, action);
    }
}
