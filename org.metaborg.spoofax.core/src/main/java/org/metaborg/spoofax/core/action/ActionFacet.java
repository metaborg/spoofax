package org.metaborg.spoofax.core.action;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.menu.IMenuItem;
import org.metaborg.util.collection.SetMultimap;

public class ActionFacet implements IFacet {
    protected final SetMultimap<ITransformGoal, ITransformAction> actions;
    public final Iterable<? extends IMenuItem> menuItems;


    public ActionFacet(SetMultimap<ITransformGoal, ITransformAction> actions, Iterable<? extends IMenuItem> menuItems) {
        this.actions = actions;
        this.menuItems = menuItems;
    }

    public Iterable<ITransformAction> actions(ITransformGoal goal) {
        return actions.get(goal);
    }

    public Iterable<ITransformGoal> goals() {
        return actions.keySet();
    }
}
