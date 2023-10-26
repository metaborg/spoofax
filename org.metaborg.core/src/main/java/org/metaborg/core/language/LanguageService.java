package org.metaborg.core.language;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.collection.Sets;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;


public class LanguageService implements ILanguageService, AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(LanguageService.class);

    private final AtomicInteger sequenceIdGenerator = new AtomicInteger(0);

    private final Map<FileName, ILanguageComponentInternal> locationToComponent = new HashMap<>();
    private final Map<LanguageIdentifier, ILanguageComponentInternal> identifierToComponent =
        new HashMap<>();
    private final Subject<LanguageComponentChange> componentChanges = PublishSubject.create();

    private final Map<LanguageIdentifier, ILanguageImplInternal> identifierToImpl = new HashMap<>();
    private final Map<String, Set<ILanguageImplInternal>> idToImpl = new HashMap<>();
    private final Subject<LanguageImplChange> implChanges = PublishSubject.create();

    private final Map<String, ILanguageInternal> nameToLanguage = new HashMap<>();

    // Added caches to ensure we always get the same ILanguage and ILanguageImpl instances,
    // even if the language (implementation) was unloaded and reloaded.
    // This fixes several issues, including issues with cached parse and analysis results that store
    // the old ILanguage(Impl) instances.
    private final ConcurrentMap<String, WeakReference<ILanguageInternal>> languageCache =
        new ConcurrentHashMap<>();
    private final ConcurrentMap<LanguageIdentifier, WeakReference<ILanguageImplInternal>>
        languageImplCache = new ConcurrentHashMap<>();


    @Override public void close() {
        languageImplCache.clear();
        languageCache.clear();
        nameToLanguage.clear();
        implChanges.onComplete();
        idToImpl.clear();
        identifierToImpl.clear();
        componentChanges.onComplete();
        identifierToComponent.clear();
        locationToComponent.clear();
    }


    @Override public @Nullable ILanguageComponent getComponent(LanguageIdentifier identifier) {
        return identifierToComponent.get(identifier);
    }

    @Override public ILanguageComponent getComponent(FileName location) {
        return locationToComponent.get(location);
    }

    @Override public @Nullable ILanguageImpl getImpl(LanguageIdentifier identifier) {
        return identifierToImpl.get(identifier);
    }

    @Override public @Nullable ILanguage getLanguage(String name) {
        return nameToLanguage.get(name);
    }


    @Override public Iterable<? extends ILanguageComponent> getAllComponents() {
        return identifierToComponent.values();
    }

    @Override public Iterable<? extends ILanguageImpl> getAllImpls() {
        return identifierToImpl.values();
    }

    @Override public Iterable<? extends ILanguageImpl> getAllImpls(String groupId, String id) {
        return idToImpl.getOrDefault(groupIdId(groupId, id), Collections.emptySet());
    }

    @Override public Iterable<? extends ILanguage> getAllLanguages() {
        return nameToLanguage.values();
    }


    @Override public Observable<LanguageComponentChange> componentChanges() {
        return componentChanges;
    }

    @Override public Observable<LanguageImplChange> implChanges() {
        return implChanges;
    }


    @Override public ILanguageComponent add(ComponentCreationConfig config) {
        validateLocation(config.location);

        final Collection<ILanguageImplInternal> impls = new LinkedList<>();
        for(LanguageContributionIdentifier identifier : config.implIds) {
            ILanguageInternal language = getOrCreateLanguage(identifier.name);
            ILanguageImplInternal impl = getOrCreateLanguageImpl(identifier.id, language);
            impls.add(impl);
        }

        final ILanguageComponentInternal existingComponent = identifierToComponent.get(config.identifier);
        final ILanguageComponentInternal newComponent =
            new LanguageComponent(config.identifier, config.location,
                sequenceIdGenerator.getAndIncrement(), impls, config.config, config.facets);
        if(existingComponent == null) {
            addComponent(newComponent);
            final Collection<ILanguageImplInternal> changedImpls = new LinkedList<>();
            for(ILanguageImplInternal impl : impls) {
                if(impl.addComponent(newComponent)) {
                    changedImpls.add(impl);
                }
            }

            componentChange(LanguageComponentChange.Kind.Add, null, newComponent);

            for(ILanguageImplInternal impl : changedImpls) {
                if(Iterables2.size(impl.components()) == 1) {
                    implChange(LanguageImplChange.Kind.Add, impl);
                } else {
                    implChange(LanguageImplChange.Kind.Reload, impl);
                }
            }
        } else {
            removeComponent(existingComponent);
            final Set<ILanguageImplInternal> removedFromImpls =
                new HashSet<ILanguageImplInternal>();
            for(ILanguageImplInternal impl : existingComponent.contributesToInternal()) {
                if(impl.removeComponent(existingComponent)) {
                    removedFromImpls.add(impl);
                }
            }
            existingComponent.clearContributions();
            addComponent(newComponent);
            final Set<ILanguageImplInternal> addedToImpls = new HashSet<ILanguageImplInternal>();
            for(ILanguageImplInternal impl : impls) {
                if(impl.addComponent(newComponent)) {
                    addedToImpls.add(impl);
                }
            }

            componentChange(LanguageComponentChange.Kind.Reload, existingComponent, newComponent);

            final Set<ILanguageImplInternal> removed = Sets.difference(removedFromImpls, addedToImpls);
            for(ILanguageImplInternal impl : removed) {
                if(Iterables2.isEmpty(impl.components())) {
                    removeImplementation(impl);
                    final ILanguageInternal language = impl.belongsToInternal();
                    language.remove(impl);
                    implChange(LanguageImplChange.Kind.Remove, impl);

                    if(Iterables2.isEmpty(language.impls())) {
                        removeLanguage(language);
                    }
                } else {
                    implChange(LanguageImplChange.Kind.Reload, impl);
                }
            }

            final Set<ILanguageImplInternal> keep = Sets.intersection(removedFromImpls, addedToImpls);
            for(ILanguageImplInternal impl : keep) {
                implChange(LanguageImplChange.Kind.Reload, impl);
            }

            final Set<ILanguageImplInternal> added = Sets.difference(addedToImpls, removedFromImpls);
            for(ILanguageImplInternal impl : added) {
                if(Iterables2.size(impl.components()) == 1) {
                    implChange(LanguageImplChange.Kind.Add, impl);
                } else {
                    implChange(LanguageImplChange.Kind.Reload, impl);
                }
            }
        }

        return newComponent;
    }

    private ILanguageInternal getOrCreateLanguage(String languageName) {
        ILanguageInternal language = nameToLanguage.get(languageName);
        if(language == null) {
            language = getCached(this.languageCache, languageName, ln -> new Language(ln));
            addLanguage(language);
        }
        assert language != null;
        return language;
    }

    private ILanguageImplInternal getOrCreateLanguageImpl(final LanguageIdentifier identifier,
        final ILanguageInternal language) {
        ILanguageImplInternal impl = identifierToImpl.get(identifier);
        if(impl == null) {
            impl = getCached(this.languageImplCache, identifier, id -> new LanguageImplementation(id, language));
            addImplementation(impl);
            language.add(impl);
        } else {
            final ILanguageInternal prevLanguage = impl.belongsToInternal();
            if(!prevLanguage.equals(language)) {
                throw new IllegalStateException(
                        "Contributions of " + identifier + " use conflicting " + prevLanguage + " and " + language);
            }
        }
        assert impl != null;
        return impl;
    }

    /**
     * This method works like {@link ConcurrentMap#computeIfAbsent(Object, Function)} except that
     *  it checks for not only null values but also whether the weak reference is empty.
     * If the cache has a still available value, that is returned, otherwise a new value is computed
     *  with the loader function, and atomically it is attempted to be added to the cache. If by
     *  then the cache was already updated, that value is used instead.
     */
    private <K, V> V getCached(ConcurrentMap<K, WeakReference<V>> cache, K languageName,
        Function<K, V> loader) {
        V language;
        WeakReference<V> v = cache.get(languageName);
        if(v == null) { // if not in the cache
            language = loader.apply(languageName); // compute new value
            v = cache.putIfAbsent(languageName, new WeakReference<>(language)); // atomically put new value
            if(v != null) { // if put failed, no old value of null returned, cache was updated in between
                final V v1 = v.get(); // get a strong reference to updated cache
                if(v1 != null) { // if this is not expired again already
                    language = v1; // we're done, this is the value from the cache
                } else {
                    return getCached(cache, languageName, loader); // bah, try again
                }
            }
        } else if(v.get() == null) { // if cached value expired
            language = loader.apply(languageName); // compute new value
            final boolean replaced = cache.replace(languageName, v, new WeakReference<>(language)); // atomically replace that expired cached value
            if(!replaced) { // cache was updated in between
                return getCached(cache, languageName, loader); // bah, try again.
            }
        } else {
            language = v.get();
        }
        return language;
    }

    @Override public void remove(ILanguageComponent component) {
        final ILanguageComponentInternal existingComponent = identifierToComponent.get(component.id());
        if(existingComponent == null) {
            throw new IllegalStateException("Cannot remove component " + component + ", it was not added before");
        }

        removeComponent(existingComponent);
        final Set<ILanguageImplInternal> removedFromImpls = new HashSet<ILanguageImplInternal>();
        for(ILanguageImplInternal impl : existingComponent.contributesToInternal()) {
            if(impl.removeComponent(existingComponent)) {
                removedFromImpls.add(impl);
            }
        }
        existingComponent.clearContributions();

        componentChange(LanguageComponentChange.Kind.Remove, existingComponent, null);

        for(ILanguageImplInternal impl : removedFromImpls) {
            if(Iterables2.isEmpty(impl.components())) {
                removeImplementation(impl);
                final ILanguageInternal language = impl.belongsToInternal();
                language.remove(impl);
                implChange(LanguageImplChange.Kind.Remove, impl);

                if(Iterables2.isEmpty(language.impls())) {
                    removeLanguage(language);
                }
            } else {
                implChange(LanguageImplChange.Kind.Reload, impl);
            }
        }
    }


    private void validateLocation(FileObject location) {
        try {
            if(!location.exists()) {
                throw new IllegalStateException(
                    "Cannot add language component at location " + location + ", location does not exist");
            }
        } catch(FileSystemException e) {
            throw new IllegalStateException("Cannot add language component at location " + location, e);
        }
    }


    private void addComponent(ILanguageComponentInternal component) {
        identifierToComponent.put(component.id(), component);
        locationToComponent.put(component.location().getName(), component);
        logger.debug("Adding {}", component);
    }

    private void addImplementation(ILanguageImplInternal impl) {
        final LanguageIdentifier id = impl.id();
        identifierToImpl.put(id, impl);
        idToImpl.computeIfAbsent(groupIdId(id), k -> new HashSet<>()).add(impl);
        logger.debug("Adding {}", impl);
    }

    private void addLanguage(ILanguageInternal language) {
        final String name = language.name();
        nameToLanguage.put(name, language);
        logger.debug("Adding {}", language);
    }

    private void removeComponent(ILanguageComponentInternal component) {
        identifierToComponent.remove(component.id());
        locationToComponent.remove(component.location().getName());
        logger.debug("Removing {}", component);
    }

    private void removeImplementation(ILanguageImplInternal impl) {
        final LanguageIdentifier id = impl.id();
        identifierToImpl.remove(id);
        idToImpl.computeIfPresent(groupIdId(id), (k, v) -> {
            v.remove(impl);
            return v.isEmpty() ? null : v;
        });
        logger.debug("Removing {}", impl);
    }

    private void removeLanguage(ILanguageInternal language) {
        final String name = language.name();
        nameToLanguage.remove(name);
        logger.debug("Removing {}", language);
    }


    private String groupIdId(String groupId, String id) {
        return groupId + ":" + id;
    }

    private String groupIdId(LanguageIdentifier identifier) {
        return groupIdId(identifier.groupId, identifier.id);
    }


    private void componentChange(LanguageComponentChange.Kind kind, ILanguageComponent oldComponent,
        ILanguageComponent newComponent) {
        componentChanges.onNext(new LanguageComponentChange(kind, oldComponent, newComponent));
    }

    private void implChange(LanguageImplChange.Kind kind, ILanguageImpl impl) {
        implChanges.onNext(new LanguageImplChange(kind, impl));
    }
}
