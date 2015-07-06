package org.metaborg.core.build;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceChange;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.util.resource.ResourceUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Fluent interface for creating {@link BuildInput} objects.
 * 
 * @see BuildInput
 */
public class BuildInputBuilder {
    private final IProject project;

    private Collection<ILanguage> languages;
    private boolean addDependencyLanguages;

    private Multimap<ILanguage, FileObject> includeLocations;
    private boolean addDefaultIncludeLocations;

    private Collection<IResourceChange> resourceChanges;
    private boolean addResourcesFromDefaultSourceLocations;

    private @Nullable FileSelector selector;

    private boolean analyze;
    private @Nullable FileSelector analyzeSelector;

    private boolean transform;
    private @Nullable FileSelector transformSelector;
    private Collection<ITransformerGoal> transformGoals;

    private @Nullable IBuildMessagePrinter messagePrinter;
    private boolean throwOnErrors;
    private Set<ILanguage> pardonedLanguages;
    private Set<String> pardonedLanguageStrings;


    @Inject public BuildInputBuilder(IProject project) {
        this.project = project;
        reset();
    }


    /**
     * Resets the builder to its original state.
     */
    public BuildInputBuilder reset() {
        languages = Lists.newLinkedList();
        addDependencyLanguages = true;
        includeLocations = HashMultimap.create();
        addDefaultIncludeLocations = true;
        resourceChanges = Lists.newLinkedList();
        addResourcesFromDefaultSourceLocations = false;
        selector = null;
        analyze = true;
        analyzeSelector = null;
        transform = true;
        transformSelector = null;
        transformGoals = Lists.newLinkedList();
        messagePrinter = null;
        throwOnErrors = false;
        pardonedLanguages = Sets.newHashSet();
        pardonedLanguageStrings = Sets.newHashSet();
        return this;
    }


    /**
     * Sets the languages to given languagues.
     */
    public BuildInputBuilder withLanguages(Collection<ILanguage> languages) {
        this.languages = languages;
        return this;
    }

    /**
     * Adds given languages.
     */
    public BuildInputBuilder addLanguages(Iterable<ILanguage> languages) {
        Iterables.addAll(this.languages, languages);
        return this;
    }

    /**
     * Adds a single language.
     */
    public BuildInputBuilder addLanguage(ILanguage language) {
        this.languages.add(language);
        return this;
    }

    /**
     * Sets if compile time dependencies should be added to languages when the input is built. Defaults to true.
     */
    public BuildInputBuilder withCompileDependencyLanguages(boolean addDependencyLanguages) {
        this.addDependencyLanguages = addDependencyLanguages;
        return this;
    }


    /**
     * Sets the include locations to given locations.
     */
    public BuildInputBuilder withIncludeLocations(Multimap<ILanguage, FileObject> includeLocations) {
        this.includeLocations = includeLocations;
        return this;
    }

    /**
     * Add given include locations for given language.
     */
    public BuildInputBuilder addIncludeLocations(ILanguage language, Iterable<FileObject> includeLocations) {
        this.includeLocations.putAll(language, includeLocations);
        return this;
    }

    /**
     * Sets if default include locations should be added when the input is build. Defaults to true.
     */
    public BuildInputBuilder withDefaultIncludeLocations(boolean addDefaultIncludeLocations) {
        this.addDefaultIncludeLocations = addDefaultIncludeLocations;
        return this;
    }


    /**
     * Sets the resource changes to given resource changes.
     */
    public BuildInputBuilder withResourceChanges(Collection<IResourceChange> resourceChanges) {
        this.resourceChanges = resourceChanges;
        return this;
    }

    /**
     * Adds a resource change.
     */
    public BuildInputBuilder addResourceChanges(Iterable<IResourceChange> resourceChanges) {
        Iterables.addAll(this.resourceChanges, resourceChanges);
        return this;
    }

    /**
     * Set the resource changes to additions from given resources.
     */
    public BuildInputBuilder withResources(Iterable<FileObject> resources) {
        this.resourceChanges = Lists.newLinkedList();
        return addResources(resources);
    }

    /**
     * Add addition resource changes from given resources.
     */
    public BuildInputBuilder addResources(Iterable<FileObject> resources) {
        for(FileObject resource : resources) {
            addResource(resource);
        }
        return this;
    }

    /**
     * Add addition resource changes from resources at given source locations.
     */
    public BuildInputBuilder addResourcesFromSourceLocations(Iterable<FileObject> sourceLocations) {
        for(FileObject sourceLocation : sourceLocations) {
            final Iterable<FileObject> sources = ResourceUtils.expand(sourceLocation);
            addResources(sources);
        }
        return this;
    }

    /**
     * Sets if addition resource changes should be added from resources at default source locations when the input is
     * built. Defaults to false.
     */
    public BuildInputBuilder withResourcesFromDefaultSourceLocations(boolean addResourcesFromDefaultSourceLocations) {
        this.addResourcesFromDefaultSourceLocations = addResourcesFromDefaultSourceLocations;
        return this;
    }

    /**
     * Adds a single addition resource change from given resource.
     */
    public BuildInputBuilder addResource(FileObject resource) {
        resourceChanges.add(new ResourceChange(resource));
        return this;
    }


    /**
     * Sets the file selector to given selector.
     */
    public BuildInputBuilder withSelector(FileSelector selector) {
        this.selector = selector;
        return this;
    }


    /**
     * Sets if analysis should be executed. Defaults to true.
     */
    public BuildInputBuilder withAnalysis(boolean analyze) {
        this.analyze = analyze;
        return this;
    }

    /**
     * Sets the analysis file selector to given selector.
     */
    public BuildInputBuilder withAnalyzeSelector(FileSelector analyzeSelector) {
        this.analyzeSelector = analyzeSelector;
        return this;
    }


    /**
     * Sets if transformations should be executed. Defaults to true.
     */
    public BuildInputBuilder withTransformation(boolean transform) {
        this.transform = transform;
        return this;
    }

    /**
     * Sets the transformation file selector to given selector.
     */
    public BuildInputBuilder withTransformSelector(FileSelector transformSelector) {
        this.transformSelector = transformSelector;
        return this;
    }

    /**
     * Sets the transform goals to given transform goals.
     */
    public BuildInputBuilder withTransformGoals(Collection<ITransformerGoal> transformGoals) {
        this.transformGoals = transformGoals;
        return this;
    }

    /**
     * Adds a single transform goal.
     */
    public BuildInputBuilder addTransformGoal(ITransformerGoal goal) {
        this.transformGoals.add(goal);
        return this;
    }


    /**
     * Sets the message printer to given message printer.
     */
    public BuildInputBuilder withMessagePrinter(IBuildMessagePrinter messagePrinter) {
        this.messagePrinter = messagePrinter;
        return this;
    }

    /**
     * Sets if a runtime exception should be thrown when errors occur. Defaults to false.
     */
    public BuildInputBuilder withThrowOnErrors(boolean throwOnErrors) {
        this.throwOnErrors = throwOnErrors;
        return this;
    }

    /**
     * Set the pardoned languages from given set of pardoned languages.
     */
    public BuildInputBuilder withPardonedLanguages(Set<ILanguage> pardonedLanguages) {
        this.pardonedLanguages = pardonedLanguages;
        return this;
    }

    /**
     * Set the pardoned languages from given language names.
     */
    public BuildInputBuilder withPardonedLanguageStrings(Iterable<String> pardonedLanguages) {
        this.pardonedLanguageStrings = Sets.newHashSet(pardonedLanguages);
        return this;
    }

    /**
     * Adds a single pardoned language.
     */
    public BuildInputBuilder addPardonedLanguage(ILanguage pardonedLanguage) {
        this.pardonedLanguages.add(pardonedLanguage);
        return this;
    }

    /**
     * Adds a single pardoned language from given language name.
     */
    public BuildInputBuilder addPardonedLanguageString(String pardonedLanguage) {
        this.pardonedLanguageStrings.add(pardonedLanguage);
        return this;
    }


    /**
     * Builds a build input object from the current state.
     */
    public BuildInput build(IDependencyService dependencyService, ILanguagePathService languagePathService) {
        final Iterable<ILanguage> compileLanguages = dependencyService.compileDependencies(project);

        if(addDependencyLanguages) {
            addLanguages(compileLanguages);
        }

        if(addDefaultIncludeLocations) {
            for(ILanguage language : compileLanguages) {
                addIncludeLocations(language, languagePathService.includes(project, language.name()));
            }
        }

        if(addResourcesFromDefaultSourceLocations) {
            for(ILanguage language : compileLanguages) {
                final Iterable<FileObject> sourceLocations = languagePathService.sources(project, language.name());
                final Iterable<FileObject> sources = ResourceUtils.expand(sourceLocations);
                addResources(sources);
            }
        }

        for(ILanguage language : languages) {
            if(pardonedLanguageStrings.contains(language.name())) {
                addPardonedLanguage(language);
            }
        }

        final BuildInput input =
            new BuildInput(project, resourceChanges, includeLocations, new BuildOrder(languages), selector, analyze,
                analyzeSelector, transform, transformSelector, transformGoals, messagePrinter, throwOnErrors,
                pardonedLanguages);
        return input;
    }

    /**
     * Builds a build input object from the current state.
     */
    public BuildInput build(Injector injector) {
        return build(injector.getInstance(IDependencyService.class), injector.getInstance(ILanguagePathService.class));
    }
}
