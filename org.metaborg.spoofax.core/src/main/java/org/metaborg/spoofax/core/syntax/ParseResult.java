package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.resource.ResourceService;

import com.google.common.collect.Lists;

public class ParseResult<T> implements Serializable {
	private static final long serialVersionUID = 7584042729127258710L;

	/**
     * Parser output, or null if parsing failed.
     */
    public @Nullable T result;

    /**
     * Resource that was parsed.
     */
    private transient FileObject source;

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

    public FileObject source() {
    	return source;
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

    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    	ResourceService.writeFileObject(source, out);
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	source = ResourceService.readFileObject(in);
    }
}
