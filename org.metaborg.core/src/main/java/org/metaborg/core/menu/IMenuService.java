package org.metaborg.core.menu;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;

public interface IMenuService {
    public abstract Iterable<IMenu> menu(ILanguageImpl language);

    public abstract @Nullable IAction action(ILanguageImpl language, String name) throws MetaborgException;

    public abstract @Nullable ActionContribution actionContribution(ILanguageImpl language, String name)
        throws MetaborgException;
}
