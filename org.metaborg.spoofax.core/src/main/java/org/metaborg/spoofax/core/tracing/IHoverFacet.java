package org.metaborg.spoofax.core.tracing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.tracing.Hover;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IHoverFacet extends IFacet {
    Hover hover(FileObject source, IContext context, ILanguageComponent contributor, Iterable<IStrategoTerm> inRegion)
        throws MetaborgException;
}
