package com.github.gogoasac.infra.input.menu;

public record MenuItem(String content, Runnable action) {

    public static MenuItem of(final String content, final Runnable action) {
        return new MenuItem(content, action);
    }
}
