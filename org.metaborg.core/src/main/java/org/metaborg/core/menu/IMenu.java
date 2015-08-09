package org.metaborg.core.menu;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;

public interface IMenu extends IMenuItem {
    public abstract Iterable<IMenuItem> items();

    public abstract @Nullable IAction action(String name) throws MetaborgException;
}
