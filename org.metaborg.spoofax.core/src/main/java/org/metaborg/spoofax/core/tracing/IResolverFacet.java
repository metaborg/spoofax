package org.metaborg.spoofax.core.tracing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.tracing.Resolution;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IResolverFacet extends IFacet {
    Resolution resolve(FileObject source, IContext context, Iterable<IStrategoTerm> inRegion,
        ILanguageComponent contributor) throws MetaborgException;
}
