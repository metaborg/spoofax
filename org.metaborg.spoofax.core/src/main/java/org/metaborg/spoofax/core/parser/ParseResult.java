package org.metaborg.spoofax.core.parser;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.IMessage;

import com.google.common.collect.Lists;

public class ParseResult<T> {
    public final T result;
    public final FileObject source;
    public final Collection<IMessage> messages;
    public final long duration;
    public final ILanguage parsedWith;


    public ParseResult(T result, FileObject source, Iterable<IMessage> messages, long duration,
        ILanguage parsedWith) {
        this.result = result;
        this.source = source;
        this.messages = Lists.newLinkedList(messages);
        this.duration = duration;
        this.parsedWith = parsedWith;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
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
        if(source == null) {
            if(other.source != null)
                return false;
        } else if(!source.equals(other.source))
            return false;

        return true;
    }

    @Override public String toString() {
        return result.toString();
    }
}
