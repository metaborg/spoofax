package org.metaborg.spoofax.core.transform;

import org.metaborg.core.transform.ITransformService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link ITransformService} with {@link IStrategoTerm}.
 */
public interface ISpoofaxTransformService extends ITransformService<IStrategoTerm, IStrategoTerm, IStrategoTerm> {

}
