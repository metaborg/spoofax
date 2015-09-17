package org.metaborg.core.menu;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Menu implements IMenu {
    private final String name;
    private final Collection<IMenuItem> items;


    public Menu(String name) {
        this(name, Lists.<IMenuItem>newLinkedList());
    }

    public Menu(String name, Collection<IMenuItem> items) {
        this.name = name;
        this.items = items;
    }


    @Override public String name() {
        return name;
    }

    @Override public Iterable<IMenuItem> items() {
        return items;
    }

    @Override public @Nullable IAction action(String name) throws MetaborgException {
        final List<IAction> actions = Lists.newLinkedList();
        for(IMenuItem item : items) {
            if(item instanceof IAction && name.equals(item.name())) {
                final IAction action = (IAction) item;
                actions.add(action);
            }
        }

        final int size = actions.size();
        if(size == 0) {
            return null;
        } else if(size > 1) {
            final String message =
                String.format("Found multiple actions with name %s: %s", name, Joiner.on(", ").join(actions));
            throw new MetaborgException(message);
        }
        return actions.get(0);
    }


    public void add(IMenuItem item) {
        items.add(item);
    }

    public void add(Iterable<? extends IMenuItem> items) {
        Iterables.addAll(this.items, items);
    }
}
