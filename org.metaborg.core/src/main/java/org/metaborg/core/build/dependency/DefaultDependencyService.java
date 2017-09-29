package org.metaborg.core.build.dependency;

import java.util.Collection;
import java.util.Collections;

import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.project.IProject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Default implementation of the {@link IDependencyService} interface.
 */
public final class DefaultDependencyService implements IDependencyService {
    private static final ILogger logger = LoggerUtils.logger(DefaultDependencyService.class);

    private final ILanguageService languageService;


    @Inject public DefaultDependencyService(ILanguageService languageService) {
        this.languageService = languageService;
    }


    @Override public Collection<ILanguageComponent> compileDeps(IProject project) throws MissingDependencyException {
        final IProjectConfig config = project.config();
        if(config.compileDeps().isEmpty()) {
            logger.trace("No compile dependencies found for project '{}'."
                + "Returning all active language components as compile dependencies instead.", project);
            return ImmutableList.copyOf(LanguageUtils.allActiveComponents(languageService));
        }
        return getLanguages(config.compileDeps());
    }

    @Override public Collection<ILanguageComponent> sourceDeps(IProject project) throws MissingDependencyException {
        final IProjectConfig config = project.config();
        if(config.sourceDeps().isEmpty()) {
            return Collections.emptyList();
        }
        return getLanguages(config.sourceDeps());
    }

    @Override public Collection<ILanguageComponent> sourceDeps(ILanguageComponent component)
        throws MissingDependencyException {
        return getLanguages(component.config().sourceDeps());
    }

    @Override public MissingDependencies checkDependencies(IProject project) {
        final IProjectConfig config = project.config();

        final Collection<LanguageIdentifier> compileDeps = config.compileDeps();
        final Collection<LanguageIdentifier> missingCompile = Lists.newLinkedList();
        for(LanguageIdentifier identifier : compileDeps) {
            if(languageService.getComponent(identifier) == null) {
                missingCompile.add(identifier);
            }
        }

        final Collection<LanguageIdentifier> sourceDeps = config.sourceDeps();
        final Collection<LanguageIdentifier> missingSource = Lists.newLinkedList();
        for(LanguageIdentifier identifier : sourceDeps) {
            if(languageService.getComponent(identifier) == null) {
                missingSource.add(identifier);
            }
        }

        return new MissingDependencies(missingCompile, missingSource);
    }

    /**
     * Gets the language components with the specified identifiers.
     *
     * @param ids
     *            The language identifiers.
     * @return A collection of language components.
     */
    private Collection<ILanguageComponent> getLanguages(Iterable<LanguageIdentifier> ids)
        throws MissingDependencyException {
        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        for(LanguageIdentifier id : ids) {
            final ILanguageComponent component = this.languageService.getComponent(id);
            if(component == null) {
                throw new MissingDependencyException(logger.format("Language for dependency {} does not exist", id));
            }
            components.add(component);
        }
        return components;
    }
}
