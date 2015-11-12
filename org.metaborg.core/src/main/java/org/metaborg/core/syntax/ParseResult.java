package org.metaborg.core.syntax;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;

import com.google.common.collect.Lists;

import java.util.Objects;

public class ParseResult<T> {
    /**
     * Parser input string.
     */
    public final String input;

    /**
     * Parser output, or null if parsing failed.
     */
    public final @Nullable T result;

    /**
     * Resource that was parsed.
     */
    public final @Nullable FileObject source;

    /**
     * Messages produced during parsing.
     */
    public final Iterable<IMessage> messages;

    /**
     * Duration of parsing in milliseconds.
     */
    public final long duration;

    /**
     * Base language the source was parsed with.
     */
    public final ILanguageImpl language;

    /**
     * Dialect the source was parsed with, or null if no dialect was used.
     */
    public final @Nullable ILanguageImpl dialect;

    /**
     * Optional parser specific data.
     */
    public final @Nullable Object parserSpecificData;


    public ParseResult(String input, @Nullable T result, @Nullable FileObject source, Iterable<IMessage> messages, long duration,
        ILanguageImpl language, @Nullable ILanguageImpl dialect, Object parserSpecificData) {
        this.input = input;
        this.result = result;
        this.source = source;
        this.messages = Lists.newLinkedList(messages);
        this.duration = duration;
        this.language = language;
        this.dialect = dialect;
        this.parserSpecificData = parserSpecificData;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int hashResult = 1;
        hashResult = prime * hashResult + ((this.result == null) ? 0 : result.hashCode());
        hashResult = prime * hashResult + ((this.source != null) ? source.hashCode() : 0);
        return hashResult;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;

        final ParseResult<?> other = (ParseResult<?>) obj;
        if(result == null) {
            if(other.result != null)
                return false;
        } else if(!result.equals(other.result))
            return false;

        return Objects.equals(this.source, other.source);
    }

    @Override public String toString() {
        if(result == null) {
            return "null";
        }
        return result.toString();
    }
}
