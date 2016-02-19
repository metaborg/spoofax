package org.metaborg.core.build.dependency;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.metaborg.core.config.ConfigException;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.IProjectConfigService;
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
    private final IProjectConfigService projectConfigService;


    @Inject public DefaultDependencyService(ILanguageService languageService,
        IProjectConfigService projectConfigService) {
        this.languageService = languageService;
        this.projectConfigService = projectConfigService;
    }


    @Override public Collection<ILanguageComponent> compileDeps(IProject project) throws MissingDependencyException {
        final IProjectConfig config = getConfig(project);
        if(config == null) {
            logger.trace("No configuration found for project '{}'."
                + "Returning all active language components as compile dependencies instead.", project);
            return ImmutableList.copyOf(LanguageUtils.allActiveComponents(languageService));
        }
        return getLanguages(config.compileDeps());
    }

    @Override public Collection<ILanguageComponent> sourceDeps(IProject project) throws MissingDependencyException {
        final IProjectConfig config = getConfig(project);
        if(config == null) {
            logger.trace("No configuration found for project '{}'. " + "Returning no runtime dependencies instead.",
                project);
            return Collections.emptyList();
        }
        return getLanguages(config.sourceDeps());
    }

    @Override public Collection<ILanguageComponent> sourceDeps(ILanguageComponent component)
        throws MissingDependencyException {
        return getLanguages(component.config().sourceDeps());
    }

    @Override public MissingDependencies checkDependencies(IProject project) {
        final IProjectConfig config = getConfig(project);
        if(config == null) {
            return new MissingDependencies();
        }

        final Collection<LanguageIdentifier> compileDeps = config.compileDeps();
        final Collection<LanguageIdentifier> missingCompile = Lists.newLinkedList();
        for(LanguageIdentifier identifier : compileDeps) {
            if(languageService.getComponentOrBaseline(identifier) == null) {
                missingCompile.add(identifier);
            }
        }

        final Collection<LanguageIdentifier> sourceDeps = config.sourceDeps();
        final Collection<LanguageIdentifier> missingSource = Lists.newLinkedList();
        for(LanguageIdentifier identifier : sourceDeps) {
            if(languageService.getComponentOrBaseline(identifier) == null) {
                missingSource.add(identifier);
            }
        }

        return new MissingDependencies(missingCompile, missingSource);
    }

    /**
     * Gets the configuration for the specified project.
     *
     * @param project
     *            The project.
     * @return The associated configuration; or <code>null</code> when there is no associated configuration or an
     *         exception occurred.
     */
    private @Nullable IProjectConfig getConfig(IProject project) {
        if(!projectConfigService.available(project)) {
            return null;
        }

        try {
            return projectConfigService.get(project);
        } catch(ConfigException e) {
            logger.debug("Exception while retrieving configuration of {}", e, project);
        }

        return null;
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
            final ILanguageComponent component = this.languageService.getComponentOrBaseline(id);
            if(component == null) {
                throw new MissingDependencyException(logger.format("Language for dependency {} does not exist", id));
            }
            components.add(component);
        }
        return components;
    }
}
