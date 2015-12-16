package org.metaborg.spoofax.core.menu;

import org.metaborg.core.language.IFacet;
import org.metaborg.core.menu.IMenuItem;

public class MenuFacet implements IFacet {
    public final Iterable<? extends IMenuItem> menuItems;


    public MenuFacet(Iterable<? extends IMenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}
