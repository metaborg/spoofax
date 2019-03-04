package org.metaborg.spoofax.core.transform;

import java.util.List;

import org.metaborg.spoofax.core.unit.TransformOutput;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TransformResult {
    public final long duration;
    public final List<TransformOutput> outputs;
    public final IStrategoTerm resultTerm;

    public TransformResult(long duration, List<TransformOutput> outputs, IStrategoTerm resultTerm) {
        this.duration = duration;
        this.outputs = outputs;
        this.resultTerm = resultTerm;
    }
}