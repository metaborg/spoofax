package org.metaborg.core.context;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class ContextService implements IContextService, IContextProcessor {
    private static final ILogger logger = LoggerUtils.logger(ContextService.class);

    private final ConcurrentMap<ContextIdentifier, IContextInternal> idToContext = Maps.newConcurrentMap();
    private final ConcurrentMap<ILanguageImpl, ContextIdentifier> langToContextId = Maps.newConcurrentMap();


    @Inject public ContextService() {
    }


    @Override public boolean available(ILanguageImpl language) {
        final ContextFacet facet = language.facet(ContextFacet.class);
        return facet != null;
    }

    @Override public IContext get(FileObject resource, ILanguageImpl language) throws ContextException {
        final ContextFacet facet = getFacet(resource, language);
        final ContextIdentifier identifier = facet.strategy.get(resource, language);
        return getOrCreate(facet.factory, identifier);
    }

    @Override public IContext get(IContext context, ILanguageImpl language) throws ContextException {
        final ContextFacet facet = getFacet(context.location(), language);
        final ContextIdentifier identifier = new ContextIdentifier(context.location(), language);
        return getOrCreate(facet.factory, identifier);
    }

    @Override public void unload(IContext context) {
        final IContextInternal contextInternal = (IContextInternal) context;
        contextInternal.unload();
        final ContextIdentifier identifier = contextInternal.identifier();
        idToContext.remove(identifier);
        langToContextId.remove(identifier.language);
    }

    @Override public void update(LanguageImplChange change) {
        switch(change.kind) {
            case Remove:
                final ContextIdentifier id = langToContextId.remove(change.impl);
                if(id != null) {
                    final IContextInternal removed = idToContext.remove(id);
                    if(removed != null) {
                        removed.unload();
                        logger.debug("Removing {}", removed);
                    }
                }
                break;
            default:
                // Ignore other changes
                break;
        }
    }


    private ContextFacet getFacet(FileObject resource, ILanguageImpl language) throws ContextException {
        final ContextFacet facet = language.facet(ContextFacet.class);
        if(facet == null) {
            final String message = logger.format("Cannot get a context, {} does not have a context facet", language);
            throw new ContextException(resource, language, message);
        }
        return facet;
    }

    private IContextInternal getOrCreate(IContextFactory factory, ContextIdentifier identifier) {
        final IContextInternal newContext = factory.create(identifier);
        final IContextInternal prevContext = idToContext.putIfAbsent(identifier, newContext);
        langToContextId.putIfAbsent(identifier.language, identifier);
        if(prevContext == null) {
            return newContext;
        }
        return prevContext;
    }
}
