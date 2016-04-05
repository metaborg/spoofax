package org.metaborg.spoofax.core.action;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.menu.IMenuItem;

import com.google.common.collect.Multimap;

public class ActionFacet implements IFacet {
    public final Multimap<ITransformGoal, ITransformAction> actions;
    public final Iterable<? extends IMenuItem> menuItems;


    public ActionFacet(Multimap<ITransformGoal, ITransformAction> actions, Iterable<? extends IMenuItem> menuItems) {
        this.actions = actions;
        this.menuItems = menuItems;
    }

    public Iterable<ITransformAction> actions(ITransformGoal goal) {
        return actions.get(goal);
    }
}
