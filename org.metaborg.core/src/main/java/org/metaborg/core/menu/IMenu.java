package org.metaborg.core.menu;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.IAction;

public interface IMenu extends IMenuItem {
    public abstract Iterable<IMenuItem> items();

    public abstract IAction action(String name) throws MetaborgException;
}
