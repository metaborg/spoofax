package org.metaborg.spoofax.core.syntax;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.IMessage;

import com.google.common.collect.Lists;

public class ParseResult<T> {
    /**
     * Parser output, or null if parsing failed.
     */
    public final @Nullable T result;

    /**
     * Resource that was parsed.
     */
    public final FileObject source;

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
    public final ILanguage language;

    /**
     * Dialect the source was parsed with, or null if no dialect was used.
     */
    public final @Nullable ILanguage dialect;


    public ParseResult(@Nullable T result, FileObject source, Iterable<IMessage> messages, long duration,
        ILanguage language, @Nullable ILanguage dialect) {
        this.result = result;
        this.source = source;
        this.messages = Lists.newLinkedList(messages);
        this.duration = duration;
        this.language = language;
        this.dialect = dialect;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int hashResult = 1;
        hashResult = prime * hashResult + ((this.result == null) ? 0 : result.hashCode());
        hashResult = prime * hashResult + source.hashCode();
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
        if(!source.equals(other.source))
            return false;

        return true;
    }

    @Override public String toString() {
        if(result == null) {
            return "null";
        }
        return result.toString();
    }
}
