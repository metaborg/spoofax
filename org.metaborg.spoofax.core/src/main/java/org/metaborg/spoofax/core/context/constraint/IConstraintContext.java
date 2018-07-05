package org.metaborg.spoofax.core.context.constraint;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.messages.IMessage;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.ImmutableList;

public interface IConstraintContext extends IContextInternal {

    enum Mode {
        SINGLE_FILE, MULTI_FILE
    }

    Mode mode();

    boolean isRoot(String resource);

    boolean isRoot(FileObject resource);

    String resourceKey(String resource);

    String resourceKey(FileObject resource);

    FileObject keyResource(String resource);

    // --- initial ---

    void setInitial(InitialResult value);

    boolean hasInitial();

    InitialResult getInitial();

    // --- final ---

    void setFinal(FinalResult value);

    boolean hasFinal();

    FinalResult getFinal();

    // --- file ---

    boolean hasUnit(String key);

    boolean hasUnit(FileObject resource);

    boolean setUnit(String key, FileResult value);

    boolean setUnit(FileObject resource, FileResult value);

    FileResult getUnit(String key);

    FileResult getUnit(FileObject resource);

    boolean remove(String key);

    boolean remove(FileObject resource);

    Set<Entry<String, FileResult>> entrySet();

    void clear();

    // --- result classes ---

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
            this.messages = ImmutableList.copyOf(messages);
        }

    }

    class FinalResult implements Serializable {

        private static final long serialVersionUID = 1L;

        public final IStrategoTerm analysis;
        public final Collection<IMessage> messages;

        public FinalResult(IStrategoTerm analysis, Collection<IMessage> messages) {
            this.analysis = analysis;
            this.messages = ImmutableList.copyOf(messages);
        }

    }

}

