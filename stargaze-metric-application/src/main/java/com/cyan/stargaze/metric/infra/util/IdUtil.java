package com.cyan.stargaze.metric.infra.util;

public final class IdUtil {

    private IdUtil() {
    }

    public static Long toLong(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String toString(Long id) {
        return id == null ? null : String.valueOf(id);
    }
}
