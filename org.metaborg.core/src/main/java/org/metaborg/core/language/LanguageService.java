package org.metaborg.core.language;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class LanguageService implements ILanguageService {
    private static final ILogger logger = LoggerUtils.logger(LanguageService.class);

    private final AtomicInteger sequenceIdGenerator = new AtomicInteger(0);

    private final Map<FileName, ILanguageComponentInternal> locationToComponent = Maps.newHashMap();
    private final Map<LanguageIdentifier, ILanguageComponentInternal> identifierToComponent = Maps.newHashMap();
    private final Subject<LanguageComponentChange, LanguageComponentChange> componentChanges = PublishSubject.create();

    private final Map<LanguageIdentifier, ILanguageImplInternal> identifierToImpl = Maps.newHashMap();
    private final SetMultimap<String, ILanguageImplInternal> idToImpl = HashMultimap.create();
    private final Subject<LanguageImplChange, LanguageImplChange> implChanges = PublishSubject.create();

    private final Map<String, ILanguageInternal> nameToLanguage = Maps.newHashMap();



    @Override public @Nullable ILanguageComponent getComponent(LanguageIdentifier identifier) {
        return identifierToComponent.get(identifier);
    }

    @Nullable
    @Override
    public ILanguageComponent getComponentOrBaseline(LanguageIdentifier identifier) {
        ILanguageComponent component = getComponent(identifier);
        if(component == null) {
            // BOOTSTRAPPING: baseline languages have version 0.0.0, try to get impl with that version.
            final LanguageIdentifier baselineIdentifier = new LanguageIdentifier(identifier, LanguageVersion.BASELINE_VERSION);
            component = getComponent(baselineIdentifier);
        }
        return component;
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
        return idToImpl.get(groupIdId(groupId, id));
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


    @Override public LanguageCreationRequest create(LanguageIdentifier identifier, FileObject location,
        Iterable<LanguageContributionIdentifier> implIds) {
        return new LanguageCreationRequest(identifier, location, implIds);
    }


    @Override public ILanguageComponent add(LanguageCreationRequest request) {
        validateLocation(request.location);

        final Collection<ILanguageImplInternal> impls = Lists.newLinkedList();
        for(LanguageContributionIdentifier identifier : request.implIds) {
            ILanguageInternal language = nameToLanguage.get(identifier.name);
            if(language == null) {
                language = new Language(identifier.name);
                addLanguage(language);
            }

            ILanguageImplInternal impl = identifierToImpl.get(identifier.identifier);
            if(impl == null) {
                impl = new LanguageImplementation(identifier.identifier, language);
                addImplementation(impl);
                language.add(impl);
            }
            impls.add(impl);
        }

        final ILanguageComponentInternal existingComponent = identifierToComponent.get(request.identifier);
        final ILanguageComponentInternal newComponent =
            new LanguageComponent(request.identifier, request.location, sequenceIdGenerator.getAndIncrement(), impls,
                request.facets);
        if(existingComponent == null) {
            addComponent(newComponent);
            final Collection<ILanguageImplInternal> changedImpls = Lists.newLinkedList();
            for(ILanguageImplInternal impl : impls) {
                if(impl.addComponent(newComponent)) {
                    changedImpls.add(impl);
                }
            }

            componentChange(LanguageComponentChange.Kind.Add, null, newComponent);

            for(ILanguageImplInternal impl : changedImpls) {
                if(Iterables.size(impl.components()) == 1) {
                    implChange(LanguageImplChange.Kind.Add, impl);
                } else {
                    implChange(LanguageImplChange.Kind.Reload, impl);
                }
            }
        } else {
            removeComponent(existingComponent);
            final Set<ILanguageImplInternal> removedFromImpls = Sets.newHashSet();
            for(ILanguageImplInternal impl : existingComponent.contributesToInternal()) {
                if(impl.removeComponent(existingComponent)) {
                    removedFromImpls.add(impl);
                }
            }
            existingComponent.clearContributions();
            addComponent(newComponent);
            final Set<ILanguageImplInternal> addedToImpls = Sets.newHashSet();
            for(ILanguageImplInternal impl : impls) {
                if(impl.addComponent(newComponent)) {
                    addedToImpls.add(impl);
                }
            }

            componentChange(LanguageComponentChange.Kind.Reload, existingComponent, newComponent);

            final Set<ILanguageImplInternal> removed = Sets.difference(removedFromImpls, addedToImpls);
            for(ILanguageImplInternal impl : removed) {
                if(Iterables.isEmpty(impl.components())) {
                    removeImplementation(impl);
                    final ILanguageInternal language = impl.belongsToInternal();
                    language.remove(impl);
                    implChange(LanguageImplChange.Kind.Remove, impl);

                    if(Iterables.isEmpty(language.impls())) {
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
                if(Iterables.size(impl.components()) == 1) {
                    implChange(LanguageImplChange.Kind.Add, impl);
                } else {
                    implChange(LanguageImplChange.Kind.Reload, impl);
                }
            }
        }

        return newComponent;
    }

    @Override public void remove(ILanguageComponent component) {
        final ILanguageComponentInternal existingComponent = identifierToComponent.get(component.id());
        if(existingComponent == null) {
            throw new IllegalStateException("Cannot remove component " + component + ", it was not added before");
        }

        removeComponent(existingComponent);
        final Set<ILanguageImplInternal> removedFromImpls = Sets.newHashSet();
        for(ILanguageImplInternal impl : existingComponent.contributesToInternal()) {
            if(impl.removeComponent(existingComponent)) {
                removedFromImpls.add(impl);
            }
        }
        existingComponent.clearContributions();

        componentChange(LanguageComponentChange.Kind.Remove, existingComponent, null);

        for(ILanguageImplInternal impl : removedFromImpls) {
            if(Iterables.isEmpty(impl.components())) {
                removeImplementation(impl);
                final ILanguageInternal language = impl.belongsToInternal();
                language.remove(impl);
                implChange(LanguageImplChange.Kind.Remove, impl);

                if(Iterables.isEmpty(language.impls())) {
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
                throw new IllegalStateException("Cannot add language component at location " + location
                    + ", location does not exist");
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
        idToImpl.put(groupIdId(id), impl);
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
        idToImpl.remove(groupIdId(id), impl);
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
