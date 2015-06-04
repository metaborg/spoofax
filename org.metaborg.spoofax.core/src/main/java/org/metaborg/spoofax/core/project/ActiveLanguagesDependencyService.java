package org.metaborg.spoofax.core.project;

import com.google.inject.Inject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.util.iterators.Iterables2;

public class ActiveLanguagesDependencyService implements IDependencyService {

    private final ILanguageService languageService;

    @Inject public ActiveLanguagesDependencyService(ILanguageService languageService) {
        this.languageService = languageService;
    }

    @Override
    public Iterable<ILanguage> runtimeDependencies(IProject project) {
        return Iterables2.empty();
    }

    @Override
    public Iterable<ILanguage> compileDependencies(IProject project) {
        return languageService.getAllActive();
    }
    
}
