package org.metaborg.spoofax.core.transform;

import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.core.transform.TransformResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class StrategoTransformerFileWriter implements IStrategoTransformerResultHandler {
    private final StrategoTransformerCommon transformer;


    @Inject public StrategoTransformerFileWriter(StrategoTransformerCommon transformer) {
        this.transformer = transformer;
    }


    @Override public void handle(TransformResult<?, IStrategoTerm> result, ITransformerGoal goal) {
        transformer.builderWriteResult(result.result, result.context.location());
    }
}
