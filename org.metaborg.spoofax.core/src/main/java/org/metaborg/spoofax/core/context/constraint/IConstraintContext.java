package org.metaborg.spoofax.core.context.constraint;

import java.util.Map.Entry;
import java.util.Set;

import org.metaborg.core.context.IContextInternal;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IConstraintContext extends IContextInternal {

    enum Mode {
        SINGLE_FILE, MULTI_FILE
    }

    Mode mode();

    boolean put(String resource, IStrategoTerm value);

    boolean remove(String resource);

    Set<Entry<String, IStrategoTerm>> entrySet();

    void clear();

}