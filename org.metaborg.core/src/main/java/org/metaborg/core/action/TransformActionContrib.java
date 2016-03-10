package org.metaborg.core.action;

import org.metaborg.core.language.ILanguageComponent;

public class TransformActionContrib {
    public final ITransformAction action;

    public final ILanguageComponent contributor;


    public TransformActionContrib(ITransformAction action, ILanguageComponent contributor) {
        this.action = action;
        this.contributor = contributor;
    }
    
    @Override public String toString() {
        return action.toString();
    }
}
