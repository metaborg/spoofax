package org.metaborg.spoofax.core.language;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.logging.log4j.Logger;
import org.metaborg.util.logging.InjectLogger;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageService implements ILanguageService {
    @InjectLogger private Logger logger;
    private final Set<ILanguageFacetFactory> facetFactories;

    private final Map<String, SortedSet<ILanguage>> nameToLanguages = Maps.newHashMap();
    private final Map<String, ILanguage> nameToActiveLanguage = Maps.newHashMap();
    private final Map<FileName, ILanguage> locationToLanguage = Maps.newHashMap();
    private final Map<String, String> extensionToLanguageName = Maps.newHashMap();
    private final Subject<LanguageChange, LanguageChange> languageChanges = PublishSubject.create();


    @Inject public LanguageService(Set<ILanguageFacetFactory> facetFactories) {
        this.facetFactories = facetFactories;
    }


    private SortedSet<ILanguage> getLanguageSet(String name) {
        SortedSet<ILanguage> set = nameToLanguages.get(name);
        if(set == null) {
            set = Sets.newTreeSet();
            nameToLanguages.put(name, set);
        }
        return set;
    }

    private boolean isActive(ILanguage language) {
        return language.equals(nameToActiveLanguage.get(language.name()));
    }

    @Override public ILanguage get(String name) {
        return nameToActiveLanguage.get(name);
    }

    @Override public ILanguage get(String name, LanguageVersion version, FileObject location) {
        final Set<ILanguage> languages = getLanguageSet(name);
        for(ILanguage language : languages) {
            if(language.version().equals(version) && language.location().equals(location)) {
                return language;
            }
        }
        return null;
    }

    @Override public ILanguage getByExt(String extension) {
        final String name = extensionToLanguageName.get(extension);
        if(name == null) {
            return null;
        }
        return get(name);
    }

    @Override public Iterable<ILanguage> getAll() {
        return Iterables.concat(nameToLanguages.values());
    }

    @Override public Iterable<ILanguage> getAllActive() {
        return nameToActiveLanguage.values();
    }

    @Override public Iterable<ILanguage> getAll(String name) {
        return getLanguageSet(name);
    }

    @Override public Iterable<ILanguage> getAll(String name, LanguageVersion version) {
        final Set<ILanguage> languages = getLanguageSet(name);
        final Collection<ILanguage> matchedLanguages = Lists.newLinkedList();
        for(ILanguage language : languages) {
            if(language.version().equals(version)) {
                matchedLanguages.add(language);
            }
        }
        return matchedLanguages;
    }

    @Override public Iterable<ILanguage> getAllByExt(String extension) {
        final String name = extensionToLanguageName.get(extension);
        if(name == null) {
            return null;
        }
        return getAll(name);
    }

    @Override public ILanguage getAny() {
        return Iterables.get(nameToActiveLanguage.values(), 0, null);
    }

    @Override public Iterable<String> getSupportedExt() {
        return extensionToLanguageName.keySet();
    }

    @Override public Observable<LanguageChange> changes() {
        return languageChanges;
    }

    private void sendLanguageChange(ILanguage language, LanguageChange.Kind kind) {
        languageChanges.onNext(new LanguageChange(language, kind));
    }

    private void load(ILanguage language, Set<ILanguage> existingLanguages) {
        try {
            if(!language.location().exists()) {
                throw new IllegalStateException("Cannot load language, location " + language.location()
                    + " does not exist.");
            }
        } catch(FileSystemException e) {
            throw new IllegalStateException("Cannot load language, could not determine if location "
                + language.location() + " exists: " + e.getMessage(), e);
        }

        for(String extension : language.extensions()) {
            final String existingName = extensionToLanguageName.get(extension);
            if(existingName != null && !existingName.equals(language.name())) {
                throw new IllegalStateException("Cannot load language, extension " + extension
                    + " is already used by language " + existingName);
            }
        }

        final ILanguage existingLanguage = locationToLanguage.get(language.location().getName());
        if(existingLanguage != null && !existingLanguage.name().equals(language.name())) {
            throw new IllegalStateException("Cannot load language, location " + language.location()
                + " is already used by language " + existingLanguage.name());
        }

        locationToLanguage.put(language.location().getName(), language);

        for(String extension : language.extensions()) {
            extensionToLanguageName.put(extension, language.name());
        }

        existingLanguages.add(language);

        sendLanguageChange(language, LanguageChange.Kind.LOADED);
    }

    private void unload(ILanguage language, Set<ILanguage> existingLanguages) {
        existingLanguages.remove(language);

        // Remove only the extensions that are not being used by any other language with the same name.
        final Set<String> unusedExtensions = Sets.newHashSet(language.extensions());
        for(ILanguage existingLanguage : existingLanguages) {
            for(String extension : existingLanguage.extensions()) {
                unusedExtensions.remove(extension);
            }
        }
        for(String extension : unusedExtensions) {
            extensionToLanguageName.remove(extension);
        }

        locationToLanguage.remove(language.location().getName());

        sendLanguageChange(language, LanguageChange.Kind.UNLOADED);
    }

    private void activate(ILanguage language) {
        nameToActiveLanguage.put(language.name(), language);

        sendLanguageChange(language, LanguageChange.Kind.ACTIVATED);
    }

    private void tryActivate(ILanguage language, SortedSet<ILanguage> existingLanguages) {
        final ILanguage activeLanguage = nameToActiveLanguage.get(language.name());

        if(activeLanguage == null) {
            activate(language);
        } else if(!isActive(language) && language.equals(existingLanguages.last())) {
            deactivate(activeLanguage);
            activate(language);
        }
    }

    private void tryActivateNew(String name, SortedSet<ILanguage> existingLanguages) {
        if(existingLanguages.isEmpty()) {
            return;
        }

        final ILanguage activeLanguage = nameToActiveLanguage.get(name);
        final ILanguage firstLanguage = existingLanguages.last();
        if(!firstLanguage.equals(activeLanguage)) {
            activate(firstLanguage);
        }
    }

    private void deactivate(ILanguage language) {
        nameToActiveLanguage.remove(language.name());

        sendLanguageChange(language, LanguageChange.Kind.DEACTIVATED);
    }

    private void tryDeactivate(ILanguage language) {
        if(isActive(language)) {
            deactivate(language);
        }
    }

    @Override public ILanguage create(String name, LanguageVersion version, FileObject location,
        ImmutableSet<String> extensions) {
        final ILanguage language = new Language(name, version, location, extensions, new Date());
        final SortedSet<ILanguage> existingLanguages = getLanguageSet(name);
        if(existingLanguages.isEmpty()) {
            // Language does not exist yet.
            load(language, existingLanguages);
            activate(language);
        } else {
            final ILanguage languageAtLocation = locationToLanguage.get(location.getName());
            if(languageAtLocation != null) {
                // Language at same location exists.
                tryDeactivate(languageAtLocation);
                unload(languageAtLocation, existingLanguages);
                load(language, existingLanguages);
                tryActivate(language, existingLanguages);
            } else {
                // Language at different location exists.
                load(language, existingLanguages);
                tryActivate(language, existingLanguages);
            }
        }

        for(ILanguageFacetFactory factory : facetFactories) {
            try {
                factory.create(language);
            } catch(Exception e) {
                logger.error("Cannot create language, creation of facets from factory " + factory.getClass()
                    + " failed: " + e.getMessage(), e);
            }
        }

        return language;
    }

    @Override public void destroy(ILanguage language) {
        final SortedSet<ILanguage> existingLanguages = getLanguageSet(language.name());
        if(existingLanguages.isEmpty()) {
            throw new IllegalStateException("Cannot remove language, language with name " + language.name()
                + " does not exist");
        }
        tryDeactivate(language);
        unload(language, existingLanguages);
        tryActivateNew(language.name(), existingLanguages);
    }
}
