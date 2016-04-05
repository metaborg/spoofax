package org.metaborg.core.analysis;

import java.util.Collection;

import org.metaborg.core.context.IContext;

import com.google.common.collect.Lists;

public class AnalyzeResult<A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> implements IAnalyzeResult<A, AU> {
    private final A result;
    private final Collection<AU> updates;
    private final IContext context;


    public AnalyzeResult(A result, Collection<AU> updates, IContext context) {
        this.result = result;
        this.updates = updates;
        this.context = context;
    }

    public AnalyzeResult(A result, IContext context) {
        this(result, Lists.<AU>newArrayList(), context);
    }


    @Override public A result() {
        return result;
    }

    @Override public Collection<AU> updates() {
        return updates;
    }

    @Override public IContext context() {
        return context;
    }
}
