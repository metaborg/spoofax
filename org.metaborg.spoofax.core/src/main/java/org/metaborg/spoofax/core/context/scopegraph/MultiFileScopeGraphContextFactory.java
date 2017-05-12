package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.meta.nabl2.config.NaBL2Config;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfig;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class MultiFileScopeGraphContextFactory implements IContextFactory {

    public static final String name = "scopegraph-multifile";

    private final Injector injector;
    private final ISpoofaxProjectConfigService configService;


    @Inject public MultiFileScopeGraphContextFactory(Injector injector, ISpoofaxProjectConfigService configService) {
        this.injector = injector;
        this.configService = configService;
    }


    @Override public IMultiFileScopeGraphContext create(ContextIdentifier identifier) {
        final ISpoofaxProjectConfig config = configService.get(identifier.project);
        final NaBL2Config nabl2Config = config != null ? config.nabl2Config() : null;
        return new MultiFileScopeGraphContext(injector, identifier, nabl2Config);
    }

    @Override public TemporaryMultiFileScopeGraphContext createTemporary(ContextIdentifier identifier) {
        return new TemporaryMultiFileScopeGraphContext(create(identifier));
    }

}
