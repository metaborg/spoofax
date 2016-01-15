package org.metaborg.core;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

/**
 * Formats message strings.
 * <p>
 * A pattern string should contain the formatting anchor <code>{}</code> wherever
 * a parameter's value should be inserted.
 * <p>
 * You can use both <code>{</code> and <code>}</code> anywhere in the pattern string
 * as long as they don't form <code>{}</code>. If for whatever reason you need to use
 * <code>{}</code> in your string, escape it by preceding it with a backslash
 * (thus <code>\{}</code>). Note that in Java source code you might have to escape
 * the backslash itself with a backslash.
 */
public final class MessageFormatter {

    // To prevent instantiation.
    private MessageFormatter() {}

    /**
     * Formats a string.
     *
     * @param pattern   The pattern, with placeholders for the parameters.
     * @param argument0 The first argument, which may be <code>null</code>.
     * @return The formatted message.
     */
    public static final String format(final String pattern, @Nullable final Object argument0) {
        Preconditions.checkNotNull(pattern);

        return org.slf4j.helpers.MessageFormatter.format(pattern, argument0).getMessage();
    }

    /**
     * Formats a string.
     *
     * @param pattern   The pattern, with placeholders for the parameters.
     * @param arguments The arguments.
     * @return The formatted message.
     */
    public static final String format(final String pattern, @Nullable final Object... arguments) {
        Preconditions.checkNotNull(pattern);

        return org.slf4j.helpers.MessageFormatter.arrayFormat(pattern, arguments).getMessage();
    }

    /**
     * Formats a string.
     *
     * @param pattern   The pattern, with placeholders for the parameters.
     * @param argument0 The first argument, which may be <code>null</code>.
     * @param argument1 The second argument, which may be <code>null</code>.
     * @return The formatted message.
     */
    public static final String format(
            final String pattern,
            @Nullable final Object argument0,
            @Nullable final Object argument1) {
        Preconditions.checkNotNull(pattern);

        return org.slf4j.helpers.MessageFormatter.format(pattern, argument0, argument1).getMessage();
    }

}
