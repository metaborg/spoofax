package org.metaborg.spoofax.core.transform.stratego;

import org.metaborg.spoofax.core.transform.ITransformerExecutor;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link ITransformerExecutor} with {@link IStrategoTerm} as type arguments.
 */
public interface IStrategoTransformerExecutor extends ITransformerExecutor<IStrategoTerm, IStrategoTerm, IStrategoTerm> {
}
