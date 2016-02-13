package org.metaborg.core.menu;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.IAction;

public interface IMenu extends IMenuItem {
    Iterable<IMenuItem> items();

    IAction action(String name) throws MetaborgException;
}
