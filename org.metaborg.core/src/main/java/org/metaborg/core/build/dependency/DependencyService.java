package org.metaborg.core.build.dependency;

import java.util.Collection;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.project.settings.IProjectSettingsService;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;


public class DependencyService implements IDependencyService {
    private static final Logger logger = LoggerFactory.getLogger(DependencyService.class);

    private final ILanguageService languageService;
    private final IProjectSettingsService projectSettingsService;


    @Inject public DependencyService(ILanguageService languageService, IProjectSettingsService projectSettingsService) {
        this.languageService = languageService;
        this.projectSettingsService = projectSettingsService;
    }


    @Override public Iterable<? extends ILanguageImpl> compileDependencies(IProject project) throws MetaborgException {
        final IProjectSettings settings = projectSettingsService.get(project);
        if(settings != null) {
            final Iterable<LanguageIdentifier> identifiers = settings.compileDependencies();
            return getLanguages(identifiers);
        }

        logger.trace("No project settings found for project {}, "
            + "using all active languages as compile dependencies", project);

        final Collection<ILanguageImpl> activeImpls = Lists.newLinkedList();
        for(ILanguage language : languageService.getAllLanguages()) {
            activeImpls.add(language.activeImpl());
        }
        return activeImpls;
    }

    @Override public Iterable<? extends ILanguageImpl> runtimeDependencies(IProject project) throws MetaborgException {
        final IProjectSettings settings = projectSettingsService.get(project);
        if(settings != null) {
            final Iterable<LanguageIdentifier> identifiers = settings.runtimeDependencies();
            return getLanguages(identifiers);
        }

        logger.trace("No project settings found for project {}, project will have no runtime dependencies", project);

        return Iterables2.<ILanguageImpl>empty();
    }

    private Iterable<? extends ILanguageImpl> getLanguages(Iterable<LanguageIdentifier> identifiers)
        throws MetaborgException {
        final Collection<ILanguageImpl> impls = Lists.newLinkedList();
        for(LanguageIdentifier identifier : identifiers) {
            ILanguageImpl impl = languageService.getImpl(identifier);
            if(impl == null) {
                // BOOTSTRAPPING: baseline languages have version 0.0.0, try to get impl with that version.
                final LanguageIdentifier baselineIdentifier =
                    new LanguageIdentifier(identifier, new LanguageVersion(0, 0, 0, ""));
                impl = languageService.getImpl(baselineIdentifier);
            }
            if(impl == null) {
                final String message = String.format("Language for dependency %s does not exist", identifier);
                throw new MetaborgException(message);
            }
            impls.add(impl);
        }
        return impls;
    }
}
