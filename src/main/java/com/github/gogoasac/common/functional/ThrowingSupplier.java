package com.github.gogoasac.common.functional;

public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
