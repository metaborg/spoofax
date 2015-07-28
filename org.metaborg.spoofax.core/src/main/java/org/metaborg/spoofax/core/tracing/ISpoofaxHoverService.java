package org.metaborg.spoofax.core.tracing;

import org.metaborg.core.tracing.IHoverService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link IHoverService} with {@link IStrategoTerm}.
 */
public interface ISpoofaxHoverService extends IHoverService<IStrategoTerm, IStrategoTerm> {

}
