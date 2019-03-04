package org.metaborg.core.action;

import org.metaborg.core.language.ILanguageComponent;

public class TransformActionContrib<TA extends ITransformAction> {
    public final TA action;
    public final ILanguageComponent contributor;


    public TransformActionContrib(TA action, ILanguageComponent contributor) {
        this.action = action;
        this.contributor = contributor;
    }

    @Override public String toString() {
        return action.toString();
    }
}
