package org.metaborg.spoofax.core.language;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LanguageService implements ILanguageService {
    private static final Logger logger = LoggerFactory.getLogger(LanguageService.class);

    /**
     * Mapping from language names to a set of all language objects with that name.
     */
    private final Map<String, Set<ILanguage>> nameToLanguages = Maps.newHashMap();
    /**
     * Mapping from language names to a sorted set of all language objects with that name. Uses
     * {@link LanguageCreationDateComparator} for sorting, such that languages created at a later date are sorted higher
     * than those created at an earlier date.
     * 
     * Note: do not use the sorted set for contains checks of languages, since it will take the creation date into
     * account.
     */
    private final Map<String, SortedSet<ILanguage>> nameToLanguagesSorted = Maps.newHashMap();
    /**
     * Mapping from locations to language objects.
     */
    private final Map<FileName, ILanguage> locationToLanguage = Maps.newHashMap();
    /**
     * Rx subject for pushing language changes.
     */
    private final Subject<LanguageChange, LanguageChange> languageChanges = PublishSubject.create();


    @Override public ILanguage get(String name) {
        return getActiveLanguage(name);
    }

    @Override public ILanguage get(FileName location) {
        return locationToLanguage.get(location);
    }

    @Override public ILanguage get(String name, LanguageVersion version, FileObject location) {
        final Iterable<ILanguage> languages = getLanguages(name);
        for(ILanguage language : languages) {
            if(language.version().equals(version) && language.location().equals(location)) {
                return language;
            }
        }
        return null;
    }

    @Override public Iterable<ILanguage> getAll() {
        return Iterables.concat(nameToLanguagesSorted.values());
    }

    @Override public Iterable<ILanguage> getAllActive() {
        final Collection<ILanguage> activeLanguages = Lists.newLinkedList();
        for(SortedSet<ILanguage> languages : nameToLanguagesSorted.values()) {
            if(!languages.isEmpty()) {
                activeLanguages.add(languages.last());
            }
        }
        return activeLanguages;
    }

    @Override public Iterable<ILanguage> getAll(String name) {
        return getLanguages(name);
    }

    @Override public Iterable<ILanguage> getAll(String name, LanguageVersion version) {
        final Collection<ILanguage> matchedLanguages = Lists.newLinkedList();
        final Iterable<ILanguage> languages = getLanguages(name);
        for(ILanguage language : languages) {
            if(language.version().equals(version)) {
                matchedLanguages.add(language);
            }
        }
        return matchedLanguages;
    }

    @Override public Observable<LanguageChange> changes() {
        return languageChanges;
    }

    @Override public ILanguage create(String name, LanguageVersion version, FileObject location) {
        logger.trace("Creating language {}", name);
        final ILanguage language = new Language(name, version, location, new Date());
        return language;
    }

    @Override public void add(ILanguage language) {
        final Set<ILanguage> languagesSet = getOrCreateLanguagesSet(language.name());
        final SortedSet<ILanguage> languagesSorted = getOrCreateLanguagesSorted(language.name());
        if(languagesSet.isEmpty()) {
            // Language does not exist at all yet.
            validateLocation(language);
            logger.debug("Loading {}", language);
            addLanguage(language, languagesSorted, languagesSet);
            sendLanguageChange(LanguageChange.Kind.ADD_FIRST, null, language);
            sendLanguageChange(LanguageChange.Kind.ADD, null, language);
        } else {
            if(languagesSet.contains(language)) {
                // Language already exists.
                if(isActive(language, languagesSorted)) {
                    logger.debug("Reloading active {}", language);
                    sendLanguageChange(LanguageChange.Kind.RELOAD_ACTIVE, language, language);
                } else {
                    logger.debug("Reloading {}", language);
                    sendLanguageChange(LanguageChange.Kind.RELOAD, language, language);
                }
            } else {
                // Language with same name exists, but not with this version or at this location.
                validateLocation(language);
                // This cannot be null, since languagesSet is not empty.
                final ILanguage oldActiveLanguage = getActiveLanguage(languagesSorted);
                final boolean activate = canBecomeActive(language, languagesSorted);
                logger.debug("Adding {}", language);
                addLanguage(language, languagesSorted, languagesSet);
                sendLanguageChange(LanguageChange.Kind.ADD, null, language);
                if(activate) {
                    logger.debug("Replacing {} with {}", oldActiveLanguage, language);
                    sendLanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, oldActiveLanguage, language);
                }
            }
        }
    }

    @Override public void remove(ILanguage language) {
        final Set<ILanguage> languagesSet = getOrCreateLanguagesSet(language.name());
        final SortedSet<ILanguage> languagesSorted = getOrCreateLanguagesSorted(language.name());
        if(languagesSet == null || languagesSet.isEmpty()) {
            throw new IllegalStateException("Cannot remove language with name " + language.name()
                + ", it was not added before");
        }
        if(!languagesSet.contains(language)) {
            throw new IllegalStateException("Cannot remove " + language + ", it was not added before");
        }

        // Remove language
        boolean wasActive = isActive(language, languagesSorted);
        removeLanguage(language, languagesSorted, languagesSet);
        sendLanguageChange(LanguageChange.Kind.REMOVE, language, null);
        if(languagesSet.size() == 0) {
            // Last language with this name.
            sendLanguageChange(LanguageChange.Kind.REMOVE_LAST, language, null);
        } else if(wasActive) {
            // This cannot be null, since languagesSet is not empty.
            final ILanguage newActiveLanguage = getActiveLanguage(languagesSorted);
            sendLanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, language, newActiveLanguage);
        }
    }


    private @Nullable SortedSet<ILanguage> getLanguagesSorted(String name) {
        return nameToLanguagesSorted.get(name);
    }

    private @Nullable Set<ILanguage> getLanguagesSet(String name) {
        return nameToLanguages.get(name);
    }

    private SortedSet<ILanguage> getOrCreateLanguagesSorted(String name) {
        SortedSet<ILanguage> sorted = getLanguagesSorted(name);
        if(sorted == null) {
            sorted = Sets.newTreeSet(new LanguageCreationDateComparator());
            nameToLanguagesSorted.put(name, sorted);
        }
        return sorted;
    }

    private Set<ILanguage> getOrCreateLanguagesSet(String name) {
        Set<ILanguage> set = getLanguagesSet(name);
        if(set == null) {
            set = Sets.newHashSet();
            nameToLanguages.put(name, set);
        }
        return set;
    }


    private Iterable<ILanguage> getLanguages(String name) {
        Iterable<ILanguage> languages = getLanguagesSorted(name);
        if(languages == null) {
            languages = Iterables2.<ILanguage>empty();
        }
        return languages;
    }

    private @Nullable ILanguage getActiveLanguage(String name) {
        final SortedSet<ILanguage> languages = getLanguagesSorted(name);
        if(languages == null) {
            return null;
        }
        return getActiveLanguage(languages);
    }

    private @Nullable ILanguage getActiveLanguage(SortedSet<ILanguage> languages) {
        if(languages.isEmpty()) {
            return null;
        }
        return languages.last();
    }


    private boolean isActive(ILanguage language, SortedSet<ILanguage> languages) {
        final ILanguage activeLanguage = getActiveLanguage(languages);
        return activeLanguage != null && language.equals(activeLanguage);
    }

    private boolean canBecomeActive(ILanguage language, SortedSet<ILanguage> languages) {
        final ILanguage activeLanguage = getActiveLanguage(languages);
        return activeLanguage == null || language.compareTo(activeLanguage) > 0;
    }

    private void validateLocation(ILanguage language) {
        try {
            if(!language.location().exists()) {
                throw new IllegalStateException("Cannot load language at location " + language.location()
                    + ", location does not exist");
            }
        } catch(FileSystemException e) {
            throw new IllegalStateException("Cannot load language at location " + language.location(), e);
        }

        final ILanguage existingLanguage = locationToLanguage.get(language.location().getName());
        if(existingLanguage != null) {
            throw new IllegalStateException("Cannot load language, location " + language.location()
                + " is already used by " + existingLanguage);
        }
    }


    private void addLanguage(ILanguage language, SortedSet<ILanguage> languagesSorted, Set<ILanguage> languages) {
        locationToLanguage.put(language.location().getName(), language);
        languagesSorted.add(language);
        languages.add(language);
    }

    private void removeLanguage(ILanguage language, SortedSet<ILanguage> languagesSorted, Set<ILanguage> languages) {
        locationToLanguage.remove(language.location().getName());
        languagesSorted.remove(language);
        languages.remove(language);
    }


    private void sendLanguageChange(LanguageChange.Kind kind, ILanguage oldLanguage, ILanguage newLanguage) {
        languageChanges.onNext(new LanguageChange(kind, oldLanguage, newLanguage));
    }
}