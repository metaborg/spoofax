package org.metaborg.core.menu;

import org.metaborg.core.MetaborgException;

public interface IMenu extends IMenuItem {
    public abstract Iterable<IMenuItem> items();

    public abstract IAction action(String name) throws MetaborgException;
}
