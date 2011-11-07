package org.ovirt.engine.core.compat;

/**
 * GWT Override for StringFormat
 */
public final class StringFormat {

    private static LogCompat log = LogFactoryCompat.getLog(StringFormat.class);

    /**
     * Format string using Java String.format() syntax (see {@link String#format(String, Object...)}) using a port of
     * java.util.Formatter
     */
    public static String format(String pattern, Object... args) {
        String message = new FormatterJava().format(pattern, args).toString();
        log.infoFormat("Formatting Java pattern: {0} With result: {1}", pattern, message);
        return message;
    }

    /**
     * Format string using DotNet string.Format() syntax (using {0} references)
     */
    public static String formatDotNet(String pattern, Object... args) {
        return new FormatterDotnet().format(pattern, args).toString();
    }

}
