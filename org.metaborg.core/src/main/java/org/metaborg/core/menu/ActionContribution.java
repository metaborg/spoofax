package org.metaborg.core.menu;

import org.metaborg.core.language.ILanguageComponent;

public class ActionContribution {
    public final IAction action;

    public final ILanguageComponent contributor;


    public ActionContribution(IAction action, ILanguageComponent contributor) {
        this.action = action;
        this.contributor = contributor;
    }
}
