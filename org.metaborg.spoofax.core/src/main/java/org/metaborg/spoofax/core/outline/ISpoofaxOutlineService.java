package org.metaborg.spoofax.core.outline;

import org.metaborg.core.outline.IOutlineService;
import org.metaborg.core.tracing.IResolverService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link IResolverService} with {@link IStrategoTerm}.
 */
public interface ISpoofaxOutlineService extends IOutlineService<IStrategoTerm, IStrategoTerm> {

}
