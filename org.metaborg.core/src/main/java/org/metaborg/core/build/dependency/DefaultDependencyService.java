package org.metaborg.core.build.dependency;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.metaborg.core.MessageFormatter;
import org.metaborg.core.language.*;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.configuration.ILanguageSpecConfig;
import org.metaborg.core.project.configuration.ILanguageSpecConfigService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Default implementation of the {@link INewDependencyService} interface.
 */
public final class DefaultDependencyService implements INewDependencyService {

    private static final ILogger logger = LoggerUtils.logger(DefaultDependencyService.class);

    private final ILanguageService languageService;
    private final ILanguageSpecConfigService languageSpecConfigService;

    @Inject
    public DefaultDependencyService(
            ILanguageService languageService,
            ILanguageSpecConfigService languageSpecConfigService) {
        this.languageService = languageService;
        this.languageSpecConfigService = languageSpecConfigService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ILanguageComponent> compileDependencies(ILanguageSpec languageSpec) throws MissingDependencyException {
        ILanguageSpecConfig config = getConfig(languageSpec);

        if(config == null) {
            logger.trace("No configuration found for language specification '{}'."
                    + "Returning all active language components as compile dependencies instead.", languageSpec);
            return ImmutableList.copyOf(LanguageUtils.allActiveComponents(languageService));
        }

        return getLanguages(config.compileDependencies());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ILanguageComponent> runtimeDependencies(ILanguageSpec languageSpec) throws MissingDependencyException {
        ILanguageSpecConfig config = getConfig(languageSpec);

        if(config == null) {
            logger.trace("No configuration found for language specification '{}'. " +
                    "Returning no runtime dependencies instead.", languageSpec);
            return Collections.emptyList();
        }

        return getLanguages(config.runtimeDependencies());
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public Collection<ILanguageComponent> compileDependencies(ILanguageComponent component) throws MissingDependencyException {
//        final DependencyFacet facet = component.facet(DependencyFacet.class);
//
//        if(facet == null) {
//            logger.trace("No dependency facet found for language component '{}'. " +
//                    "Returning all active language components as compile dependencies instead.", component);
//            return ImmutableList.copyOf(LanguageUtils.allActiveComponents(languageService));
//        }
//
//        return getLanguages(facet.compileDependencies);
//    }

    /**
     * {@inheritDoc}
     */
    @Override public Collection<ILanguageComponent> runtimeDependencies(ILanguageComponent component) throws MissingDependencyException {

        final DependencyFacet facet = component.facet(DependencyFacet.class);
        if(facet == null) {
            logger.trace("No dependency facet found for language component '{}'. " +
                    "Returning no runtime dependencies instead.", component);
            return Collections.emptyList();
        }

        return getLanguages(facet.runtimeDependencies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MissingDependencies checkDependencies(ILanguageSpec languageSpec) {
        ILanguageSpecConfig config = getConfig(languageSpec);
        if (config == null) {
            return new MissingDependencies();
        }

        final Collection<LanguageIdentifier> compile = config.compileDependencies();
        final Collection<LanguageIdentifier> missingCompile = Lists.newLinkedList();
        for(LanguageIdentifier identifier : compile) {
            if(this.languageService.getComponentOrBaseline(identifier) == null) {
                missingCompile.add(identifier);
            }
        }

        final Collection<LanguageIdentifier> runtime = config.runtimeDependencies();
        final Collection<LanguageIdentifier> missingRuntime = Lists.newLinkedList();
        for(LanguageIdentifier identifier : runtime) {
            if(this.languageService.getComponentOrBaseline(identifier) == null) {
                missingRuntime.add(identifier);
            }
        }

        return new MissingDependencies(missingCompile, missingRuntime);
    }

    /**
     * Gets the configuration for the specified language specification.
     *
     * @param languageSpec The language specification.
     * @return The associated configuration; or <code>null</code> when
     * there is no associated configuration or an exception occurred.
     */
    @Nullable
    private ILanguageSpecConfig getConfig(ILanguageSpec languageSpec) {
        ILanguageSpecConfig config = null;
        try {
            config = this.languageSpecConfigService.get(languageSpec);
        } catch (IOException e) {
            logger.error("Exception while retrieving configuration of {}.", languageSpec, e);
        }
        return config;
    }

    /**
     * Gets the language components with the specified identifiers.
     *
     * @param identifiers The language identifiers.
     * @return A collection of language components.
     */
    private Collection<ILanguageComponent> getLanguages(final Iterable<LanguageIdentifier> identifiers) throws MissingDependencyException {
        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        for(LanguageIdentifier identifier : identifiers) {
            final ILanguageComponent component = this.languageService.getComponentOrBaseline(identifier);
            if(component == null) {
                throw new MissingDependencyException(MessageFormatter.format("Language for dependency {} does not exist.", identifier));
            }
            components.add(component);
        }
        return components;
    }
}
