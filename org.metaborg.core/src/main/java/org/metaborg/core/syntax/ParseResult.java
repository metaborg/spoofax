package org.metaborg.core.syntax;

import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.IResult;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;

import com.google.common.collect.Lists;

public class ParseResult<P> implements IResult<P> {
    /**
     * Parser input string.
     */
    public final String input;

    /**
     * Parser output, or null if parsing failed.
     */
    public final @Nullable P result;

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


    public ParseResult(String input, @Nullable P result, @Nullable FileObject source, Iterable<IMessage> messages, long duration,
                       ILanguageImpl language, @Nullable ILanguageImpl dialect, @Nullable Object parserSpecificData) {
        this.input = input;
        this.result = result;
        this.source = source;
        this.messages = Lists.newLinkedList(messages);
        this.duration = duration;
        this.language = language;
        this.dialect = dialect;
        this.parserSpecificData = parserSpecificData;
    }

    
    @Override public @Nullable P value() {
        return result;
    }

    @Override public Iterable<IMessage> messages() {
        return messages;
    }

    @Override public long duration() {
        return duration;
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
