package org.metaborg.spoofax.core.menu;

import org.metaborg.core.language.IFacet;
import org.metaborg.core.menu.IMenu;

public class MenuFacet implements IFacet {
    public final Iterable<IMenu> menus;

    
    public MenuFacet(Iterable<IMenu> menus) {
        this.menus = menus;
    }
}
