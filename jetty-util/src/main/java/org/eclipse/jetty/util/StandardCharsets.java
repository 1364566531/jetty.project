package org.eclipse.jetty.util;

import java.nio.charset.Charset;

/**
 * Replacement of java.nio.charset.StandardCharsets for non-Java 1.7 (Android).
 */
public final class StandardCharsets {
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
    public static final Charset UTF_16 = Charset.forName("UTF-16");
}
