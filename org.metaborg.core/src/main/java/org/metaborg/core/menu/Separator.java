package org.metaborg.core.menu;

public class Separator implements IMenuItem {
    @Override public String name() {
        return "";
    }

    @Override public void accept(final IMenuItemVisitor visitor) {
        visitor.visitSeparator(this);
    }
}
