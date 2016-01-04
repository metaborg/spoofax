package org.metaborg.core.action;

import org.metaborg.core.language.ILanguageComponent;

public class TransformActionContribution {
    public final ITransformAction action;

    public final ILanguageComponent contributor;


    public TransformActionContribution(ITransformAction action, ILanguageComponent contributor) {
        this.action = action;
        this.contributor = contributor;
    }
}
