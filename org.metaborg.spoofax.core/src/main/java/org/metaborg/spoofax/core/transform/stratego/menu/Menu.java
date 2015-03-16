package org.metaborg.spoofax.core.transform.stratego.menu;

import java.util.Collection;

import com.google.common.collect.Lists;

public class Menu {
    private final String name;
    private final Collection<Menu> submenus;
    private final Collection<Action> actions;


    public Menu(String name) {
        this(name, Lists.<Menu>newLinkedList(), Lists.<Action>newLinkedList());
    }

    public Menu(String name, Collection<Menu> submenus, Collection<Action> actions) {
        this.name = name;
        this.submenus = submenus;
        this.actions = actions;
    }


    public String name() {
        return name;
    }

    public Iterable<Menu> submenus() {
        return submenus;
    }

    public Iterable<Action> actions() {
        return actions;
    }

    public void add(Menu submenu) {
        submenus.add(submenu);
    }

    public void add(Action action) {
        actions.add(action);
    }
}
