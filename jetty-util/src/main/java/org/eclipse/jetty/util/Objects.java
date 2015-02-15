package org.eclipse.jetty.util;

/**
 * Replacement of java.util.Objects for non-Java 1.7 (Android).
 */
public final class Objects {
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }

    public static <T> T requireNonNull(T obj, String msg) {
        if (obj == null)
            throw new NullPointerException(msg);
        return obj;
    }

}
