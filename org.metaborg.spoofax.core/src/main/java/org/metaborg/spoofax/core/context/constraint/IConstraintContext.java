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


    default boolean isRoot(String resource) {
        return isRoot(keyResource(resource));
    }

    boolean isRoot(FileObject resource);


    default boolean hasAnalysis(String resource) {
        return hasAnalysis(keyResource(resource));
    }

    boolean hasAnalysis(FileObject resource);


    default IStrategoTerm getAnalysis(String resource) {
        return getAnalysis(keyResource(resource));
    }

    IStrategoTerm getAnalysis(FileObject resource);


    default String resourceKey(String resource) {
        return resourceKey(keyResource(resource));
    }

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

    default boolean hasUnit(String resource) {
        return hasUnit(keyResource(resource));
    }

    boolean hasUnit(FileObject resource);


    default boolean setUnit(String resource, FileResult value) {
        return setUnit(keyResource(resource), value);
    }

    boolean setUnit(FileObject resource, FileResult value);


    default FileResult getUnit(String resource) {
        return getUnit(keyResource(resource));
    }

    FileResult getUnit(FileObject resource);


    default boolean remove(String resource) {
        return remove(keyResource(resource));
    }

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

