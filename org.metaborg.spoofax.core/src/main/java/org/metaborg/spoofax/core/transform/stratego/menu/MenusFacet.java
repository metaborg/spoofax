package org.metaborg.spoofax.core.transform.stratego.menu;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.language.ILanguageFacet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MenusFacet implements ILanguageFacet {
    private static final long serialVersionUID = -2695936811141361719L;
    
	private final Collection<Menu> menus;
    private final Map<String, Action> actions = Maps.newHashMap();

    public MenusFacet() {
        this(Lists.<Menu>newLinkedList());
    }

    public MenusFacet(Collection<Menu> menus) {
        this.menus = menus;
        mapActions(this.menus);
    }


    public Iterable<Menu> menus() {
        return menus;
    }

    public @Nullable Action action(String name) {
        return actions.get(name);
    }

    public void add(Menu menu) {
        menus.add(menu);
        mapActions(menu);
    }

    private void mapActions(Menu menu) {
        for(Action action : menu.actions()) {
            actions.put(action.name, action);
        }
        mapActions(menu.submenus());
    }

    private void mapActions(Iterable<Menu> menus) {
        for(Menu submenu : menus) {
            mapActions(submenu);
        }
    }
}
