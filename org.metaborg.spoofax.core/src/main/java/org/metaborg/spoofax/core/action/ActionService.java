package org.metaborg.spoofax.core.action;

import java.util.Collection;

import org.metaborg.core.action.IActionService;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformAction;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ActionService implements IActionService<ISpoofaxTransformAction> {
    @Override public Collection<ISpoofaxTransformAction> actions(ILanguageImpl language, ITransformGoal goal) {
        final Iterable<ActionFacet> facets = language.facets(ActionFacet.class);
        final Collection<ISpoofaxTransformAction> actions = Lists.newLinkedList();
        for(ActionFacet facet : facets) {
            Iterables.addAll(actions, facet.actions(goal));
        }
        return actions;
    }

    @Override public Collection<TransformActionContrib<ISpoofaxTransformAction>> actionContributions(ILanguageImpl language,
        ITransformGoal goal) {
        final Iterable<FacetContribution<ActionFacet>> facetsContributions =
            language.facetContributions(ActionFacet.class);
        final Collection<TransformActionContrib<ISpoofaxTransformAction>> actionContributions = Lists.newLinkedList();
        for(FacetContribution<ActionFacet> facetContribution : facetsContributions) {
            final ActionFacet facet = facetContribution.facet;
            final ILanguageComponent component = facetContribution.contributor;
            for(ISpoofaxTransformAction action : facet.actions(goal)) {
                actionContributions.add(new TransformActionContrib<ISpoofaxTransformAction>(action, component));
            }
        }
        return actionContributions;
    }

    @Override public boolean available(ILanguageImpl language, ITransformGoal goal) {
        return !actions(language, goal).isEmpty();
    }

    @Override public boolean requiresAnalysis(ILanguageImpl language, ITransformGoal goal) {
        for(ISpoofaxTransformAction action : actions(language, goal)) {
            if(!action.flags().parsed) {
                return true;
            }
        }
        return false;
    }
}
