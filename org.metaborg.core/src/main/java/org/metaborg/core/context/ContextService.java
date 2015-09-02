package org.metaborg.core.context;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageImplChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class ContextService implements IContextService, IContextProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ContextService.class);

    private final IContextFactory contextFactory;

    private final ConcurrentMap<ContextIdentifier, IContextInternal> idToContext = Maps.newConcurrentMap();
    private final ConcurrentMap<ILanguageImpl, ContextIdentifier> langToContextId = Maps.newConcurrentMap();


    @Inject public ContextService(IContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }


    @Override public IContext get(FileObject resource, ILanguageImpl language) throws ContextException {
        final ContextFacet facet = getFacet(resource, language);
        final IContextStrategy strategy = facet.strategy();
        final ContextIdentifier identifier = strategy.get(resource, language);
        return getOrCreate(identifier);
    }

    @Override public IContext get(IContext context, ILanguageImpl language) throws ContextException {
        final ContextIdentifier identifier = new ContextIdentifier(context.location(), language);
        return getOrCreate(identifier);
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
            final String message = String.format("Cannot get a context, %s does not have a context facet", language);
            logger.error(message);
            throw new ContextException(resource, language, message);
        }
        return facet;
    }

    private IContextInternal getOrCreate(ContextIdentifier identifier) {
        final IContextInternal newContext = contextFactory.create(identifier);
        final IContextInternal prevContext = idToContext.putIfAbsent(identifier, newContext);
        langToContextId.putIfAbsent(identifier.language, identifier);
        if(prevContext == null) {
            return newContext;
        }
        return prevContext;
    }
}
