package org.metaborg.core.context;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.project.IProject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import javax.inject.Inject;
import com.google.inject.Injector;

public class ContextService implements IContextService, IContextProcessor {
    private static final ILogger logger = LoggerUtils.logger(ContextService.class);

    private final Injector injector;

    private final ConcurrentMap<ContextIdentifier, IContextInternal> idToContext = Maps.newConcurrentMap();
    private final ConcurrentMap<ILanguageImpl, ContextIdentifier> langToContextId = Maps.newConcurrentMap();
    private final Multimap<ILanguageImpl, ContextIdentifier> langToDownstreamContextIds = Multimaps.synchronizedMultimap(HashMultimap.create());

    @Inject public ContextService(Injector injector) {
        this.injector = injector;
    }


    @Override public IContext get(FileObject resource, IProject project, ILanguageImpl language)
        throws ContextException {
        if(available(language)) {
            final ContextFacet facet = getFacet(resource, language);
            final ContextIdentifier identifier = facet.strategy.get(resource, project, language);
            return getOrCreate(facet.factory, identifier);
        } else {
            return createNullContext(project, language);
        }
    }

    @Override public ITemporaryContext getTemporary(FileObject resource, IProject project, ILanguageImpl language)
        throws ContextException {
        if(available(language)) {
            final ContextFacet facet = getFacet(resource, language);
            ContextIdentifier identifier;
            try {
                identifier = facet.strategy.get(resource, project, language);
            } catch(ContextException e) {
                logger.debug("Could not create a context via context strategy of language {} (see exception)"
                    + ", creating context with given resource {} instead", e, language, resource);
                identifier = new ContextIdentifier(resource, project, language);
            }
            return createTemporary(facet.factory, identifier);
        } else {
            return createNullContext(project, language);
        }
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
                final ContextIdentifier removedId = langToContextId.remove(change.impl);
                if(removedId != null) {
                    final IContextInternal removed = idToContext.remove(removedId);
                    if(removed != null) {
                        removed.unload();
                        logger.debug("Removing {}", removed);
                    }
                }
                break;
            case Reload:
                for(ContextIdentifier reloadedId : langToDownstreamContextIds.get(change.impl)) {
                    final IContextInternal reloaded = idToContext.remove(reloadedId);
                    if(reloaded != null) {
                        try {
                            reloaded.reset();
                            logger.debug("Resetting {}", reloaded);
                        } catch(IOException e) {
                            logger.debug("Resetting {} failed.", e, reloaded);
                        }
                    }
                }
            default:
                // Ignore other changes
                break;
        }
    }


    private boolean available(ILanguageImpl language) {
        final ContextFacet facet = language.facet(ContextFacet.class);
        return facet != null;
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
        final IContextInternal newContext = create(factory, identifier);
        final IContextInternal prevContext = idToContext.putIfAbsent(identifier, newContext);
        langToContextId.putIfAbsent(identifier.language, identifier);
        langToDownstreamContextIds.put(newContext.language(), identifier);
        if(prevContext == null) {
            return newContext;
        } else if (!prevContext.getClass().equals(newContext.getClass())) {
            /* If the language implementation changed its context settings, we
             * should really return the new context, and discard the previous.
             */
            logger.info("Context for {} changed type, ignoring existing context.", identifier);
            if(idToContext.replace(identifier, prevContext, newContext)) {
                try {
                    prevContext.reset();
                } catch (IOException e) {
                    logger.warn("Error occurred while resetting {}",prevContext,e);
                }
            } else {
                logger.warn("Race condition while replacing context {} with {}",prevContext,newContext);
            }
            langToDownstreamContextIds.remove(prevContext.language(), identifier);
            return newContext;
        }
        return prevContext;
    }

    private IContextInternal create(IContextFactory factory, ContextIdentifier identifier) {
        return factory.create(identifier);
    }

    private ITemporaryContextInternal createTemporary(IContextFactory factory, ContextIdentifier identifier) {
        return factory.createTemporary(identifier);
    }

    private NullContext createNullContext(IProject project, ILanguageImpl language) {
        return new NullContext(project.location(), project, language, injector);
    }

}
