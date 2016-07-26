package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IScopeGraphUnit {
    FileObject source();
    IStrategoTerm constraint();
    @Nullable IStrategoTerm analysis();
}
