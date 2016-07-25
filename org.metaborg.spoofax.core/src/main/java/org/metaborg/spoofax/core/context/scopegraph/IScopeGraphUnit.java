package org.metaborg.spoofax.core.context.scopegraph;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IScopeGraphUnit {

    public FileObject source();
    public IStrategoTerm constraint();
    
}
