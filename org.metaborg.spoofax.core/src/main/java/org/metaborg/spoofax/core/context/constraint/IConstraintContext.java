package org.metaborg.spoofax.core.context.constraint;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.messages.IMessage;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IConstraintContext extends IContextInternal {

    enum Mode {
        SINGLE_FILE, MULTI_FILE
    }

    Mode mode();

    // --- initial ---

    void setInitial(InitialResult value);

    boolean hasInitial();

    InitialResult getInitial();

    // --- final ---

    void setFinal(FinalResult value);

    boolean hasFinal();

    FinalResult getFinal();

    // --- file ---

    boolean contains(String key);

    default boolean contains(FileObject resource) {
        return contains(resourceKey(resource));
    }

    boolean put(String key, FileResult value);

    default boolean put(FileObject resource, FileResult value) {
        return put(resourceKey(resource), value);
    }

    FileResult get(String key);

    default FileResult get(FileObject resource) {
        return get(resourceKey(resource));
    }

    boolean remove(String key);

    default boolean remove(FileObject resource) {
        return remove(resourceKey(resource));
    }

    Set<Entry<String, FileResult>> entrySet();

    void clear();

    FileObject keyResource(String key) throws IOException;

    String resourceKey(FileObject resource);

    class InitialResult implements Serializable {

        private static final long serialVersionUID = 1L;

        public final IStrategoTerm analysis;

        public InitialResult(IStrategoTerm analysis) {
            this.analysis = analysis;
        }

    }

    class FileResult implements Serializable {

        private static final long serialVersionUID = 1L;

        public final IStrategoTerm ast;
        public final IStrategoTerm analysis;
        public final Collection<IMessage> messages;

        public FileResult(IStrategoTerm ast, IStrategoTerm analysis, Collection<IMessage> messages) {
            this.ast = ast;
            this.analysis = analysis;
            this.messages = messages;
        }

    }

    class FinalResult implements Serializable {

        private static final long serialVersionUID = 1L;

        public final IStrategoTerm analysis;
        public final Collection<IMessage> messages;

        public FinalResult(IStrategoTerm analysis, Collection<IMessage> messages) {
            this.analysis = analysis;
            this.messages = messages;
        }

    }
}

