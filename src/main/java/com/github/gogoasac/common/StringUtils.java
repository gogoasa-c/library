package com.github.gogoasac.common;

import com.github.gogoasac.common.functional.ThrowingSupplier;

public final class StringUtils {
    public static final String EMPTY_STRING = "";

    private StringUtils() {}

    public static String orElse(final ThrowingSupplier<String> stringSupplier, final String defaultValue) {
        try {
            final String result = stringSupplier.get();

            return result == null ? defaultValue : result;
        } catch (Exception exception) {
            return defaultValue;
        }
    }
}
