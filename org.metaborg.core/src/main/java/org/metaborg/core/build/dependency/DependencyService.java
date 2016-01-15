package org.metaborg.core.build.dependency;

import java.util.Collection;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.project.settings.IProjectSettingsService;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Deprecated
public class DependencyService implements IDependencyService {
    private static final ILogger logger = LoggerUtils.logger(DependencyService.class);

    private final ILanguageService languageService;
    private final IProjectSettingsService projectSettingsService;


    @Inject public DependencyService(ILanguageService languageService, IProjectSettingsService projectSettingsService) {
        this.languageService = languageService;
        this.projectSettingsService = projectSettingsService;
    }


    @Override public Iterable<ILanguageComponent> compileDependencies(IProject project) throws MetaborgException {
        final IProjectSettings settings = projectSettingsService.get(project);
        if(settings != null) {
            final Iterable<LanguageIdentifier> identifiers = settings.compileDependencies();
            return getLanguages(identifiers);
        }

        logger.trace("No project settings found for project {}, "
            + "using all active components as compile dependencies", project);

        return LanguageUtils.allActiveComponents(languageService);
    }

    @Override public Iterable<ILanguageComponent> runtimeDependencies(IProject project) throws MetaborgException {
        final IProjectSettings settings = projectSettingsService.get(project);
        if(settings != null) {
            final Iterable<LanguageIdentifier> identifiers = settings.runtimeDependencies();
            return getLanguages(identifiers);
        }

        logger.trace("No project settings found for project {}, project will have no runtime dependencies", project);

        return Iterables2.empty();
    }

    @Override public MissingDependencies checkDependencies(IProject project) {
        final IProjectSettings settings = projectSettingsService.get(project);
        if(settings != null) {
            final Iterable<LanguageIdentifier> compile = settings.compileDependencies();
            final Collection<LanguageIdentifier> missingCompile = Lists.newLinkedList();
            for(LanguageIdentifier identifier : compile) {
                if(getLanguage(identifier) == null) {
                    missingCompile.add(identifier);
                }
            }
            
            final Iterable<LanguageIdentifier> runtime = settings.runtimeDependencies();
            final Collection<LanguageIdentifier> missingRuntime = Lists.newLinkedList();
            for(LanguageIdentifier identifier : runtime) {
                if(getLanguage(identifier) == null) {
                    missingRuntime.add(identifier);
                }
            }
            return new MissingDependencies(missingCompile, missingRuntime);
        }
        return new MissingDependencies();
    }


    @Override public Iterable<ILanguageComponent> compileDependencies(ILanguageComponent component)
        throws MetaborgException {
        final DependencyFacet facet = component.facet(DependencyFacet.class);
        if(facet != null) {
            return getLanguages(facet.compileDependencies);
        }

        logger
            .trace("No dependency facet found for {}, using all active components as compile dependencies", component);

        return LanguageUtils.allActiveComponents(languageService);
    }


    @Override public Iterable<ILanguageComponent> runtimeDependencies(ILanguageComponent component)
        throws MetaborgException {
        final DependencyFacet facet = component.facet(DependencyFacet.class);
        if(facet != null) {
            return getLanguages(facet.runtimeDependencies);
        }

        logger.trace("No dependency facet found for {}, component will have no runtime dependencies", component);

        return Iterables2.empty();
    }


    private Iterable<ILanguageComponent> getLanguages(Iterable<LanguageIdentifier> identifiers)
        throws MetaborgException {
        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        for(LanguageIdentifier identifier : identifiers) {
            final ILanguageComponent component = getLanguage(identifier);
            if(component == null) {
                final String message = String.format("Language for dependency %s does not exist", identifier);
                throw new MetaborgException(message);
            }
            components.add(component);
        }
        return components;
    }
    
    private @Nullable ILanguageComponent getLanguage(LanguageIdentifier identifier) {
        ILanguageComponent component = languageService.getComponent(identifier);
        if(component == null) {
            // BOOTSTRAPPING: baseline languages have version 0.0.0, try to get impl with that version.
            final LanguageIdentifier baselineIdentifier =
                new LanguageIdentifier(identifier, new LanguageVersion(0, 0, 0, ""));
            component = languageService.getComponent(baselineIdentifier);
        }
        return component;
    }
}
