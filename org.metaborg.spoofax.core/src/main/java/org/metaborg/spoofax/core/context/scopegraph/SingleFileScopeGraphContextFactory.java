package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.meta.nabl2.config.NaBL2Config;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfig;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class SingleFileScopeGraphContextFactory implements IContextFactory {

    public static final String name = "scopegraph-singlefile";

    private final Injector injector;
    private final ISpoofaxProjectConfigService configService;


    @Inject public SingleFileScopeGraphContextFactory(Injector injector, ISpoofaxProjectConfigService configService) {
        this.injector = injector;
        this.configService = configService;
    }


    @Override public ISingleFileScopeGraphContext create(ContextIdentifier identifier) {
        final ISpoofaxProjectConfig config = configService.get(identifier.project);
        final NaBL2Config nabl2Config = config != null ? config.nabl2Config() : null;
        return new SingleFileScopeGraphContext(injector, identifier, nabl2Config);
    }

    @Override public TemporarySingleFileScopeGraphContext createTemporary(ContextIdentifier identifier) {
        return new TemporarySingleFileScopeGraphContext(create(identifier));
    }

}
