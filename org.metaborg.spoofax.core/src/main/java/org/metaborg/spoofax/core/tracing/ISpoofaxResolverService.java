package org.metaborg.spoofax.core.tracing;

import org.metaborg.core.tracing.IResolverService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link IResolverService} with {@link IStrategoTerm}.
 */
public interface ISpoofaxResolverService extends IResolverService<IStrategoTerm, IStrategoTerm> {

}
