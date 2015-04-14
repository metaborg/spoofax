package org.metaborg.spoofax.core.context;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class ContextService implements IContextService {
    private static final Logger logger = LoggerFactory.getLogger(ContextService.class);

    private final IContextFactory contextFactory;

    private final ConcurrentMap<ContextIdentifier, IContextInternal> contexts = Maps.newConcurrentMap();


    @Inject public ContextService(IContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }


    @Override public IContext get(FileObject resource, ILanguage language) throws ContextException {
        final ContextFacet facet = language.facet(ContextFacet.class);
        if(facet == null) {
            final String message = String.format("Cannot get a context, % does not have a context facet", language);
            logger.error(message);
            throw new SpoofaxRuntimeException(message);
        }

        final IContextStrategy strategy = facet.strategy();
        final ContextIdentifier identifier = strategy.get(resource, language);
        final IContextInternal newContext = contextFactory.create(identifier);
        final IContextInternal prevContext = contexts.putIfAbsent(identifier, newContext);
        if(prevContext == null) {
            newContext.initialize();
            return newContext;
        }
        return prevContext;
    }

    @Override public void unload(IContext context) {
        final IContextInternal contextInternal = (IContextInternal) context;
        contextInternal.unload();
        final ContextIdentifier identifier = contextInternal.identifier();
        contexts.remove(identifier);
    }
}
