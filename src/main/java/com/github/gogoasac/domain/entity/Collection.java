package com.github.gogoasac.domain.entity;

public record Collection(
    Long id,
    String name) {

    @Override
    public String toString() {
        return String.format("Collection{id=%s, name='%s'}", id, name);
    }
}
