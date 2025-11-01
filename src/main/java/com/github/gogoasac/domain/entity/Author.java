package com.github.gogoasac.domain.entity;

public record Author(
    Long id,
    String name) {

    @Override
    public String toString() {
        return String.format("Author{id=%s, name='%s'}", id, name);
    }
}
