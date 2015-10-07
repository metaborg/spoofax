package org.metaborg.core.menu;

import org.metaborg.core.transform.NestedNamedGoal;

public interface IAction extends IMenuItem {
    public abstract NestedNamedGoal goal();
}
