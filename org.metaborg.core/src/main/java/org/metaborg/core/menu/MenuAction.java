package org.metaborg.core.menu;

import org.metaborg.core.action.ITransformAction;

public class MenuAction implements IMenuAction {
    public final ITransformAction action;


    public MenuAction(ITransformAction action) {
        this.action = action;
    }


    @Override public String name() {
        return action.name();
    }

    @Override public ITransformAction action() {
        return action;
    }

    @Override public void accept(IMenuItemVisitor visitor) {
        visitor.visitAction(this);
    }
}
