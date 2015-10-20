package org.metaborg.core.menu;

import org.metaborg.core.transform.NestedNamedGoal;

/**
 * Base class for actions.
 */
public abstract class Action implements IAction {

    @Override
    public abstract String name();

    @Override
    public abstract NestedNamedGoal goal();

    @Override
    public void accept(final IMenuItemVisitor visitor) {
        visitor.visitAction(this);
    }
}
