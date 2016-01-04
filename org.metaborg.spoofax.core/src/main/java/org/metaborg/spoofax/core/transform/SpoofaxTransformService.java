package org.metaborg.spoofax.core.transform;

import org.metaborg.core.transform.ITransformer;
import org.metaborg.core.transform.TransformService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Typedef class for {@link TransformService} with {@link IStrategoTerm}.
 */
public class SpoofaxTransformService extends TransformService<IStrategoTerm, IStrategoTerm, IStrategoTerm> implements
    ISpoofaxTransformService {
    @Inject public SpoofaxTransformService(ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer) {
        super(transformer);
    }
}
