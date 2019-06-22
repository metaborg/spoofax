package org.metaborg.spoofax.core.action;

import com.google.common.collect.Multimap;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.menu.IMenuItem;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformAction;

public class ActionFacet implements IFacet {
    public final Multimap<ITransformGoal, ISpoofaxTransformAction> actions;
    public final Iterable<? extends IMenuItem> menuItems;


    public ActionFacet(Multimap<ITransformGoal, ISpoofaxTransformAction> actions, Iterable<? extends IMenuItem> menuItems) {
        this.actions = actions;
        this.menuItems = menuItems;
    }

    public Iterable<ISpoofaxTransformAction> actions(ITransformGoal goal) {
        return actions.get(goal);
    }
}
