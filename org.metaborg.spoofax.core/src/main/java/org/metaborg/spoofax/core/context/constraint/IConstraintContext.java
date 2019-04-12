package org.metaborg.spoofax.core.context.constraint;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContextInternal;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IConstraintContext extends IContextInternal {

    enum Mode {
        SINGLE_FILE, MULTI_FILE
    }

    Mode mode();


    default boolean isRoot(String resource) {
        return isRoot(keyResource(resource));
    }

    boolean isRoot(FileObject resource);

    FileObject root();

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


    default boolean contains(String resource) {
        return contains(keyResource(resource));
    }

    boolean contains(FileObject resource);


    default boolean put(String resource, IStrategoTerm value) {
        return put(keyResource(resource), value);
    }

    boolean put(FileObject resource, IStrategoTerm value);


    default IStrategoTerm get(String resource) {
        return get(keyResource(resource));
    }

    IStrategoTerm get(FileObject resource);


    default boolean remove(String resource) {
        return remove(keyResource(resource));
    }

    boolean remove(FileObject resource);


    Set<Entry<String, IStrategoTerm>> entrySet();

    void clear();

}