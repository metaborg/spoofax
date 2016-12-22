package org.metaborg.spoofax.core.context.scopegraph;

public class TemporarySingleFileScopeGraphContext extends AbstractTemporaryScopeGraphContext<ISingleFileScopeGraphUnit>
        implements ISingleFileScopeGraphContext {

    public TemporarySingleFileScopeGraphContext(ISingleFileScopeGraphContext context) {
        super(context);
    }

}