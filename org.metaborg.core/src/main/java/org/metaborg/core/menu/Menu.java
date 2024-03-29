package org.metaborg.core.menu;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.IAction;
import org.metaborg.util.Strings;
import org.metaborg.util.iterators.Iterables2;

public class Menu implements IMenu {
    private final String name;
    private final Collection<IMenuItem> items;


    public Menu(String name) {
        this(name, new LinkedList<>());
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
        final List<IAction> actions = new LinkedList<>();
        for(IMenuItem item : items) {
            if(item instanceof IMenuAction && name.equals(item.name())) {
                final IMenuAction menuAction = (IMenuAction) item;
                final IAction action = menuAction.action();
                actions.add(action);
            }
        }

        final int size = actions.size();
        if(size == 0) {
            return null;
        } else if(size > 1) {
            final String message =
                String.format("Found multiple actions with name %s: %s", name, Strings.tsJoin(actions, ", "));
            throw new MetaborgException(message);
        }
        return actions.get(0);
    }


    public void add(IMenuItem item) {
        items.add(item);
    }

    public void add(Iterable<? extends IMenuItem> items) {
        Iterables2.addAll(this.items, items);
    }

    @Override public void accept(final IMenuItemVisitor visitor) {
        visitor.visitMenu(this);
    }
}
