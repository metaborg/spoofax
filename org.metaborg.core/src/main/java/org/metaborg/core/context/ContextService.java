package org.metaborg.core.context;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.project.IProject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Injector;

public class ContextService implements IContextService, IContextProcessor {
    private static final ILogger logger = LoggerUtils.logger(ContextService.class);

    private final Injector injector;

    private final ConcurrentMap<ContextIdentifier, IContextInternal> idToContext = new ConcurrentHashMap<>();
    private final ConcurrentMap<ILanguageImpl, ContextIdentifier> langToContextId = new ConcurrentHashMap<>();
    private final ConcurrentMap<ILanguageImpl, Set<ContextIdentifier>> langToDownstreamContextIds = new ConcurrentHashMap<>();

    @jakarta.inject.Inject @javax.inject.Inject public ContextService(Injector injector) {
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
                for(ContextIdentifier reloadedId : getLangToDownstreamContextId(change)) {
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
        putLangToDownstreamContextId(identifier, newContext.language());
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
            removeLangToDownstreamContextId(identifier, prevContext.language());
            return newContext;
        }
        return prevContext;
    }

    private Collection<ContextIdentifier> getLangToDownstreamContextId(LanguageImplChange change) {
        return Collections.unmodifiableSet(langToDownstreamContextIds.getOrDefault(change.impl, Collections.emptySet()));
    }

    private boolean putLangToDownstreamContextId(ContextIdentifier identifier, ILanguageImpl language) {
        return langToDownstreamContextIds.computeIfAbsent(language, k -> ConcurrentHashMap.newKeySet()).add(identifier);
    }

    private boolean removeLangToDownstreamContextId(ContextIdentifier identifier, ILanguageImpl language) {
        final AtomicBoolean wasPresent = new AtomicBoolean(false);
        langToDownstreamContextIds.computeIfPresent(language, (k, v) -> {
            wasPresent.set(v.remove(identifier));
            return v; // N.B. we do _not_ remove empty sets, this is to avoid potential concurrency issues
        });
        return wasPresent.get();
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
