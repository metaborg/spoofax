package org.metaborg.spoofax.core.context.constraint;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.spoofax.core.analysis.constraint.IResourceKey;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IConstraintContext extends IContextInternal {

    enum Mode {
        SINGLE_FILE, MULTI_FILE
    }

    Mode mode();


    default boolean isRoot(IResourceKey resource) {
        return isRoot(keyResource(resource));
    }

    boolean isRoot(FileObject resource);

    FileObject root();

    default boolean hasAnalysis(IResourceKey resource) {
        return hasAnalysis(keyResource(resource));
    }

    boolean hasAnalysis(FileObject resource);


    default IStrategoTerm getAnalysis(IResourceKey resource) {
        return getAnalysis(keyResource(resource));
    }

    IStrategoTerm getAnalysis(FileObject resource);


    default IResourceKey resourceKey(IResourceKey resource) {
        return resourceKey(keyResource(resource));
    }

    IResourceKey resourceKey(FileObject resource);

    FileObject keyResource(IResourceKey resource);


    default boolean contains(IResourceKey resource) {
        return contains(keyResource(resource));
    }

    boolean contains(FileObject resource);


    default boolean put(IResourceKey resource, IStrategoTerm value) {
        return put(keyResource(resource), value);
    }

    boolean put(FileObject resource, IStrategoTerm value);


    default IStrategoTerm get(IResourceKey resource) {
        return get(keyResource(resource));
    }

    IStrategoTerm get(FileObject resource);


    default boolean remove(IResourceKey resource) {
        return remove(keyResource(resource));
    }

    boolean remove(FileObject resource);


    Set<Entry<IResourceKey, IStrategoTerm>> entrySet();

    void clear();

}