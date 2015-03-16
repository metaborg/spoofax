package org.metaborg.spoofax.core.transform;

public interface ITransformerResultHandler<TransT> {
    public abstract void handle(TransformResult<?, TransT> result, ITransformerGoal goal);
}
