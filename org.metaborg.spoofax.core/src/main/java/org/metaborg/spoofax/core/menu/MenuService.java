package org.metaborg.spoofax.core.menu;

import java.util.Collection;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.menu.ActionContribution;
import org.metaborg.core.menu.IAction;
import org.metaborg.core.menu.IMenu;
import org.metaborg.core.menu.IMenuService;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class MenuService implements IMenuService {
    @Override public Iterable<IMenu> menu(ILanguageImpl language) {
        final Iterable<MenuFacet> facets = language.facets(MenuFacet.class);
        final Collection<IMenu> menus = Lists.newLinkedList();
        for(MenuFacet facet : facets) {
            Iterables.addAll(menus, facet.menus);
        }
        return menus;
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
            final Iterable<IMenu> menus = facet.menus;
            for(IMenu menu : menus) {
                final IAction action = menu.action(name);
                if(action != null) {
                    actions.put(facetContrib.contributor, action);
                }
            }
        }

        final int size = actions.size();
        if(size == 0) {
            return null;
        } else if(size > 1) {
            final String message =
                String.format("Found multiple actions with name %s: %s", name, Joiner.on(", ").join(actions.values()));
            throw new MetaborgException(message);
        }
        final Entry<ILanguageComponent, IAction> entry = Iterables.get(actions.entries(), 0);
        return new ActionContribution(entry.getValue(), entry.getKey());
    }
}
