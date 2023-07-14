package org.metaborg.core.analysis;

import java.util.ArrayList;
import java.util.Collection;

import org.metaborg.core.context.IContext;

public class AnalyzeResults<A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> implements IAnalyzeResults<A, AU> {
    private final Collection<A> results;
    private final Collection<AU> updates;
    private final IContext context;


    public AnalyzeResults(Collection<A> results, Collection<AU> updates, IContext context) {
        this.results = results;
        this.updates = updates;
        this.context = context;
    }

    public AnalyzeResults(Collection<A> results, IContext context) {
        this(results, new ArrayList<AU>(), context);
    }

    public AnalyzeResults(IContext context) {
        this(new ArrayList<A>(), context);
    }


    @Override public Collection<A> results() {
        return results;
    }

    @Override public Collection<AU> updates() {
        return updates;
    }

    @Override public IContext context() {
        return context;
    }
}
