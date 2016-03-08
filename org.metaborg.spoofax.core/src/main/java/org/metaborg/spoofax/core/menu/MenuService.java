package org.metaborg.spoofax.core.menu;

import java.util.List;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.menu.IMenu;
import org.metaborg.core.menu.IMenuItem;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.spoofax.core.action.ActionFacet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MenuService implements IMenuService {
    @Override public Iterable<IMenuItem> menuItems(ILanguageImpl language) {
        final Iterable<ActionFacet> facets = language.facets(ActionFacet.class);
        final List<IMenuItem> menuItems = Lists.newLinkedList();
        for(ActionFacet facet : facets) {
            Iterables.addAll(menuItems, facet.menuItems);
        }
        if(menuItems.size() == 1) {
            final IMenuItem item = menuItems.get(0);
            if(item instanceof IMenu) {
                final IMenu menu = (IMenu) item;
                return menu.items();
            }
        }
        return menuItems;
    }

}
