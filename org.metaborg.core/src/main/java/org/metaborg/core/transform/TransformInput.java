package org.metaborg.core.transform;

import org.metaborg.core.context.IContext;

public class TransformInput<PrevT, TransT> {
    public final TransT input;
    public final IContext context;
    public final ITransformerGoal goal;
    public final PrevT previousResult;


    public TransformInput(TransT input, IContext context, ITransformerGoal goal, PrevT previousResult) {
        this.input = input;
        this.context = context;
        this.goal = goal;
        this.previousResult = previousResult;
    }
}
