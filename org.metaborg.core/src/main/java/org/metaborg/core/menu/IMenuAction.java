package org.metaborg.core.menu;

import org.metaborg.core.action.ITransformAction;

public interface IMenuAction extends IMenuItem {
    public abstract ITransformAction action();
}
