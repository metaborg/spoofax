package org.metaborg.spoofax.core.transform.stratego;

import org.metaborg.spoofax.core.transform.ITransformer;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link ITransformer} with {@link IStrategoTerm} as type arguments.
 */
public interface IStrategoTransformer extends ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> {

}
