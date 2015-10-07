package org.metaborg.spoofax.core.menu;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.menu.ActionContribution;
import org.metaborg.core.menu.IAction;
import org.metaborg.core.menu.IMenu;
import org.metaborg.core.menu.IMenuItem;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class MenuService implements IMenuService {
    @Override public Iterable<IMenuItem> menuItems(ILanguageImpl language) {
        final Iterable<MenuFacet> facets = language.facets(MenuFacet.class);
        final List<IMenuItem> menuItems = Lists.newLinkedList();
        for(MenuFacet facet : facets) {
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


    @Override public @Nullable IAction nestedAction(ILanguageImpl language, List<String> names)
        throws MetaborgException {
        final ActionContribution actionContrib = nestedActionContribution(language, names);
        if(actionContrib == null) {
            return null;
        }
        return actionContrib.action;
    }

    @Override public @Nullable ActionContribution nestedActionContribution(ILanguageImpl language, List<String> names)
        throws MetaborgException {
        final Iterable<FacetContribution<MenuFacet>> facets = language.facetContributions(MenuFacet.class);
        final Multimap<ILanguageComponent, IAction> actions = ArrayListMultimap.create();
        for(FacetContribution<MenuFacet> facetContrib : facets) {
            final MenuFacet facet = facetContrib.facet;
            for(IMenuItem menuItem : facet.menuItems) {
                final Iterable<IAction> foundActions = findNestedActions(menuItem, names);
                actions.putAll(facetContrib.contributor, foundActions);
            }
        }

        final int size = actions.size();
        if(size == 0) {
            return null;
        } else if(size > 1) {
            final String path = Joiner.on(" -> ").join(names);
            final String message = String.format("Found multiple actions with name %s", path);
            throw new MetaborgException(message);
        }
        final Entry<ILanguageComponent, IAction> entry = Iterables.get(actions.entries(), 0);
        return new ActionContribution(entry.getValue(), entry.getKey());
    }

    private Iterable<IAction> findNestedActions(IMenuItem item, List<String> names) {
        if(names.isEmpty()) {
            return Iterables2.empty();
        }

        final String name = names.get(0);
        if(name.equals(item.name())) {
            final List<String> rest = names.subList(1, names.size());
            if(rest.isEmpty() && item instanceof IAction) {
                return Iterables2.singleton((IAction) item);
            } else if(item instanceof IMenu) {
                final IMenu menu = (IMenu) item;
                final Collection<IAction> actions = Lists.newLinkedList();
                for(IMenuItem nestedItem : menu.items()) {
                    final Iterable<IAction> foundActions = findNestedActions(nestedItem, rest);
                    Iterables.addAll(actions, foundActions);
                }
                return actions;
            }
        }

        return Iterables2.empty();
    }


    @Override public @Nullable IAction action(ILanguageImpl language, String name) throws MetaborgException {
        final ActionContribution actionContrib = actionContribution(language, name);
        if(actionContrib == null) {
            return null;
        }
        return actionContrib.action;
    }

    @Override public @Nullable ActionContribution actionContribution(ILanguageImpl language, String name)
        throws MetaborgException {
        final Iterable<FacetContribution<MenuFacet>> facets = language.facetContributions(MenuFacet.class);
        final Multimap<ILanguageComponent, IAction> actions = ArrayListMultimap.create();
        for(FacetContribution<MenuFacet> facetContrib : facets) {
            final MenuFacet facet = facetContrib.facet;
            for(IMenuItem menuItem : facet.menuItems) {
                final Iterable<IAction> foundActions = findActions(menuItem, name);
                actions.putAll(facetContrib.contributor, foundActions);
            }
        }

        final int size = actions.size();
        if(size == 0) {
            return null;
        } else if(size > 1) {
            final String message = String.format("Found multiple actions with name %s", name);
            throw new MetaborgException(message);
        }
        final Entry<ILanguageComponent, IAction> entry = Iterables.get(actions.entries(), 0);
        return new ActionContribution(entry.getValue(), entry.getKey());
    }

    private Iterable<IAction> findActions(IMenuItem item, String name) {
        if(name.equals(item.name()) && item instanceof IAction) {
            return Iterables2.singleton((IAction) item);
        } else if(item instanceof IMenu) {
            final IMenu menu = (IMenu) item;
            final Collection<IAction> actions = Lists.newLinkedList();
            for(IMenuItem nestedItem : menu.items()) {
                final Iterable<IAction> foundActions = findActions(nestedItem, name);
                Iterables.addAll(actions, foundActions);
            }
            return actions;
        }

        return Iterables2.empty();
    }
}
