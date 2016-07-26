package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.ITemporaryContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IScopeGraphContext extends IContext, ITemporaryContext {
    @Nullable IScopeGraphInitial initial();
    Map<FileObject,IScopeGraphUnit> units();
    @Nullable IStrategoTerm analysis();
}
