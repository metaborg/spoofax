package org.metaborg.spoofax.core.context;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.ITemporaryContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IScopeGraphContext extends IContext, ITemporaryContext {

    @Nullable IStrategoTerm getInitial();
 
    Collection<FileObject> sources();
 
}
