package org.metaborg.spoofax.core.language;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

public class LanguageService implements ILanguageService {
    private static final Logger logger = LoggerFactory.getLogger(LanguageService.class);

    /**
     * Atomic integer for generating monotonically increasing sequence identifiers.
     */
    private final AtomicInteger sequenceIdGenerator = new AtomicInteger(0);
    /**
     * Mapping from language names to a set of all language objects with that name.
     */
    private final SetMultimap<String, ILanguage> nameToLanguages = HashMultimap.create();
    /**
     * Mapping from language identifiers to a set of all language objects with that name.
     */
    private final SetMultimap<String, ILanguage> idToLanguages = HashMultimap.create();
    /**
     * Mapping from locations to language objects.
     */
    private final Map<FileName, ILanguage> locationToLanguage = Maps.newHashMap();
    /**
     * Rx subject for pushing language changes.
     */
    private final Subject<LanguageChange, LanguageChange> languageChanges = PublishSubject.create();


    @Override public @Nullable ILanguage get(String name) {
        return getActiveLanguage(nameToLanguages.get(name));
    }

    @Override public @Nullable ILanguage get(FileName location) {
        return locationToLanguage.get(location);
    }

    @Override public @Nullable ILanguage get(String name, LanguageVersion version, FileObject location) {
        final Iterable<ILanguage> languages = nameToLanguages.get(name);
        for(ILanguage language : languages) {
            if(language.version().equals(version) && language.location().equals(location)) {
                return language;
            }
        }
        return null;
    }

    @Override public @Nullable ILanguage getWithId(String id) {
        return getActiveLanguage(idToLanguages.get(id));
    }

    @Override public @Nullable ILanguage getWithId(String id, LanguageVersion version) {
        final Iterable<ILanguage> languages = idToLanguages.get(id);
        for(ILanguage language : languages) {
            if(language.version().equals(version)) {
                return language;
            }
        }
        return null;
    }

    @Override public Iterable<ILanguage> getAll() {
        return nameToLanguages.values();
    }

    @Override public Iterable<ILanguage> getAllActive() {
        final Collection<ILanguage> activeLanguages = Lists.newLinkedList();
        for(Collection<ILanguage> languages : nameToLanguages.asMap().values()) {
            if(!languages.isEmpty()) {
                activeLanguages.add(getActiveLanguage(languages));
            }
        }
        return activeLanguages;
    }

    @Override public Iterable<ILanguage> getAll(String name) {
        return nameToLanguages.get(name);
    }

    @Override public Iterable<ILanguage> getAll(String name, LanguageVersion version) {
        final Collection<ILanguage> matchedLanguages = Lists.newLinkedList();
        final Iterable<ILanguage> languages = nameToLanguages.get(name);
        for(ILanguage language : languages) {
            if(language.version().equals(version)) {
                matchedLanguages.add(language);
            }
        }
        return matchedLanguages;
    }

    @Override public Iterable<ILanguage> getAllWithId(String id, LanguageVersion version) {
        final Collection<ILanguage> matchedLanguages = Lists.newLinkedList();
        final Iterable<ILanguage> languages = idToLanguages.get(id);
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

    @Override public ILanguage create(String name, LanguageVersion version, FileObject location, String id) {
        logger.trace("Creating language {}", name);
        final ILanguage language = new Language(name, location, version, sequenceIdGenerator.getAndIncrement(), id);
        return language;
    }

    @Override public void add(ILanguage language) {
        final Set<ILanguage> languages = nameToLanguages.get(language.name());
        if(languages.isEmpty()) {
            // Language does not exist at all yet.
            validateLocation(language);
            logger.debug("Loading {}", language);
            addLanguage(language);
            sendLanguageChange(LanguageChange.Kind.ADD_FIRST, null, language);
            sendLanguageChange(LanguageChange.Kind.ADD, null, language);
        } else {
            // Cannot be null, languagesSet is not empty.
            final ILanguage activeLanguage = getActiveLanguage(languages);

            if(languages.contains(language)) {
                // Language already exists.
                // Cannot be null, languagesSet contains language.
                final ILanguage reloadedLanguage = getEqualLanguage(languages, language);
                removeLanguage(reloadedLanguage);
                addLanguage(language);
                if(isActive(language, languages)) {
                    logger.debug("Reloading active {}", language);
                    sendLanguageChange(LanguageChange.Kind.RELOAD_ACTIVE, reloadedLanguage, language);
                } else {
                    logger.debug("Reloading {}", language);
                    sendLanguageChange(LanguageChange.Kind.RELOAD, reloadedLanguage, language);
                }
            } else {
                // Language with same name exists, but not with this version or at this location.
                validateLocation(language);
                final boolean activate = canBecomeActive(language, languages);
                logger.debug("Adding {}", language);
                addLanguage(language);
                sendLanguageChange(LanguageChange.Kind.ADD, null, language);
                if(activate) {
                    logger.debug("Replacing {} with {}", activeLanguage, language);
                    sendLanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, activeLanguage, language);
                }
            }
        }
    }

    @Override public void remove(ILanguage language) {
        final Set<ILanguage> languages = nameToLanguages.get(language.name());
        if(languages == null || languages.isEmpty()) {
            throw new IllegalStateException("Cannot remove language with name " + language.name()
                + ", it was not added before");
        }
        if(!languages.contains(language)) {
            throw new IllegalStateException("Cannot remove " + language + ", it was not added before");
        }

        // Remove language
        boolean wasActive = isActive(language, languages);
        removeLanguage(language);
        sendLanguageChange(LanguageChange.Kind.REMOVE, language, null);
        if(languages.size() == 0) {
            // Last language with this name.
            sendLanguageChange(LanguageChange.Kind.REMOVE_LAST, language, null);
        } else if(wasActive) {
            // Cannot be null, languagesSet is not empty.
            final ILanguage newActiveLanguage = getActiveLanguage(languages);
            sendLanguageChange(LanguageChange.Kind.REPLACE_ACTIVE, language, newActiveLanguage);
        }
    }

    private boolean isGreater(ILanguage language, ILanguage other) {
        int compareVersion = language.version().compareTo(other.version());
        if(compareVersion > 0 || (compareVersion == 0 && language.sequenceId() > other.sequenceId())) {
            return true;
        }
        return false;
    }

    private @Nullable ILanguage getActiveLanguage(Iterable<ILanguage> languages) {
        ILanguage activeLanguage = null;
        for(ILanguage language : languages) {
            if(activeLanguage == null || isGreater(language, activeLanguage)) {
                activeLanguage = language;
            }

        }
        return activeLanguage;
    }

    private boolean isActive(ILanguage language, Iterable<ILanguage> languages) {
        final ILanguage activeLanguage = getActiveLanguage(languages);
        return activeLanguage != null && language.equals(activeLanguage);
    }

    private boolean canBecomeActive(ILanguage language, Iterable<ILanguage> languages) {
        final ILanguage activeLanguage = getActiveLanguage(languages);
        return activeLanguage == null || isGreater(language, activeLanguage);
    }

    private @Nullable ILanguage getEqualLanguage(Iterable<ILanguage> languages, ILanguage language) {
        for(ILanguage equalLanguage : languages) {
            if(equalLanguage.equals(language)) {
                return equalLanguage;
            }
        }
        return null;
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


    private void addLanguage(ILanguage language) {
        locationToLanguage.put(language.location().getName(), language);
        nameToLanguages.put(language.name(), language);
        idToLanguages.put(language.id(), language);
    }

    private void removeLanguage(ILanguage language) {
        locationToLanguage.remove(language.location().getName());
        nameToLanguages.remove(language.name(), language);
        idToLanguages.remove(language.id(), language);
    }


    private void sendLanguageChange(LanguageChange.Kind kind, ILanguage oldLanguage, ILanguage newLanguage) {
        languageChanges.onNext(new LanguageChange(kind, oldLanguage, newLanguage));
    }
}