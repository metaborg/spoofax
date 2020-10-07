package org.metaborg.spoofax.core.context.constraint;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContextInternal;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IConstraintContext extends IContextInternal {

    default String resourceKey(String resource) {
        return resourceKey(keyResource(resource));
    }

    String resourceKey(FileObject resource);

    FileObject keyResource(String resource);


    default boolean contains(String resource) {
        return contains(keyResource(resource));
    }

    boolean contains(FileObject resource);


    default boolean hasChanged(String resource, int parseHash) {
        return hasChanged(keyResource(resource), parseHash);
    }

    boolean hasChanged(FileObject resource, int parseHash);


    default boolean put(String resource, int parseHash, IStrategoTerm analyzedAst, IStrategoTerm analysis,
            IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, List<String> exceptions) {
        return put(keyResource(resource), parseHash, analyzedAst, analysis, errors, warnings, notes, exceptions);
    }

    boolean put(FileObject resource, int parseHash, IStrategoTerm analyzedAst, IStrategoTerm analysis,
            IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, List<String> exceptions);


    default Entry get(String resource) {
        return get(keyResource(resource));
    }

    Entry get(FileObject resource);


    default boolean remove(String resource) {
        return remove(keyResource(resource));
    }

    boolean remove(FileObject resource);


    Set<Map.Entry<String, Entry>> entrySet();

    void clear();


    interface Entry {

        int parseHash();

        IStrategoTerm analyzedAst();

        IStrategoTerm analysis();

        IStrategoTerm errors();

        IStrategoTerm warnings();

        IStrategoTerm notes();

        List<String> exceptions();

    }

}