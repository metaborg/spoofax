package org.metaborg.spoofax.core.unit;

import org.metaborg.core.transform.ITransformOutput;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface ISpoofaxTransformOutput extends ITransformOutput {
    /**
     * The output term of the transformation.
     */
    IStrategoTerm ast();
}