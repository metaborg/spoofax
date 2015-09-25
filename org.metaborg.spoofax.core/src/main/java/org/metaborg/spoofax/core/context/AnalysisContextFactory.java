package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.spoofax.core.terms.ITermFactoryService;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class AnalysisContextFactory implements IContextFactory {
    public static final String name = "analysis";
    
    private final Injector injector;
    private final ITermFactoryService termFactoryService;


    @Inject public AnalysisContextFactory(Injector injector, ITermFactoryService termFactoryService) {
        this.injector = injector;
        this.termFactoryService = termFactoryService;
    }


    @Override public IContextInternal create(ContextIdentifier identifier) {
        return new AnalysisContext(injector, termFactoryService, identifier);
    }
}
