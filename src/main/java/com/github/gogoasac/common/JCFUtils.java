package com.github.gogoasac.common;

import java.util.Collection;

public final class JCFUtils {
    private JCFUtils() {}

    public static boolean isEmptyOrNull(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
