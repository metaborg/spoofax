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


    public BuildInputBuilder withLanguages(Collection<ILanguage> languages) {
        this.languages = languages;
        return this;
    }

    public BuildInputBuilder addLanguages(Iterable<ILanguage> languages) {
        Iterables.addAll(this.languages, languages);
        return this;
    }

    public BuildInputBuilder addLanguage(ILanguage language) {
        this.languages.add(language);
        return this;
    }

    public BuildInputBuilder withCompileDependencyLanguages(boolean addDependencyLanguages) {
        this.addDependencyLanguages = addDependencyLanguages;
        return this;
    }


    public BuildInputBuilder withIncludeLocations(Multimap<ILanguage, FileObject> includeLocations) {
        this.includeLocations = includeLocations;
        return this;
    }

    public BuildInputBuilder addIncludeLocations(ILanguage language, Iterable<FileObject> includeLocations) {
        this.includeLocations.putAll(language, includeLocations);
        return this;
    }

    public BuildInputBuilder withDefaultIncludeLocations(boolean addDefaultIncludeLocations) {
        this.addDefaultIncludeLocations = addDefaultIncludeLocations;
        return this;
    }


    public BuildInputBuilder withResourceChanges(Collection<IResourceChange> resourceChanges) {
        this.resourceChanges = resourceChanges;
        return this;
    }

    public BuildInputBuilder addResourceChanges(Iterable<IResourceChange> resourceChanges) {
        Iterables.addAll(this.resourceChanges, resourceChanges);
        return this;
    }

    public BuildInputBuilder withResources(Iterable<FileObject> resources) {
        this.resourceChanges = Lists.newLinkedList();
        return addResources(resources);
    }

    public BuildInputBuilder addResources(Iterable<FileObject> resources) {
        for(FileObject resource : resources) {
            addResource(resource);
        }
        return this;
    }

    public BuildInputBuilder addResourcesFromSourceLocations(Iterable<FileObject> sourceLocations) {
        for(FileObject sourceLocation : sourceLocations) {
            final Iterable<FileObject> sources = ResourceUtils.expand(sourceLocation);
            addResources(sources);
        }
        return this;
    }

    public BuildInputBuilder withResourcesFromDefaultSourceLocations(boolean addResourcesFromDefaultSourceLocations) {
        this.addResourcesFromDefaultSourceLocations = addResourcesFromDefaultSourceLocations;
        return this;
    }

    public BuildInputBuilder addResource(FileObject resource) {
        resourceChanges.add(new ResourceChange(resource));
        return this;
    }


    public BuildInputBuilder withSelector(FileSelector selector) {
        this.selector = selector;
        return this;
    }


    public BuildInputBuilder withAnalysis(boolean analyze) {
        this.analyze = analyze;
        return this;
    }

    public BuildInputBuilder withAnalyzeSelector(FileSelector analyzeSelector) {
        this.analyzeSelector = analyzeSelector;
        return this;
    }


    public BuildInputBuilder withTransformation(boolean transform) {
        this.transform = transform;
        return this;
    }

    public BuildInputBuilder withTransformSelector(FileSelector transformSelector) {
        this.transformSelector = transformSelector;
        return this;
    }

    public BuildInputBuilder withTransformGoals(Collection<ITransformerGoal> transformGoals) {
        this.transformGoals = transformGoals;
        return this;
    }

    public BuildInputBuilder addGoal(ITransformerGoal goal) {
        this.transformGoals.add(goal);
        return this;
    }


    public BuildInputBuilder withMessagePrinter(IBuildMessagePrinter messagePrinter) {
        this.messagePrinter = messagePrinter;
        return this;
    }

    public BuildInputBuilder withThrowOnErrors(boolean throwOnErrors) {
        this.throwOnErrors = throwOnErrors;
        return this;
    }

    public BuildInputBuilder withPardonedLanguages(Set<ILanguage> pardonedLanguages) {
        this.pardonedLanguages = pardonedLanguages;
        return this;
    }

    public BuildInputBuilder withPardonedLanguageStrings(Iterable<String> pardonedLanguages) {
        this.pardonedLanguageStrings = Sets.newHashSet(pardonedLanguages);
        return this;
    }

    public BuildInputBuilder addPardonedLanguage(ILanguage pardonedLanguage) {
        this.pardonedLanguages.add(pardonedLanguage);
        return this;
    }

    public BuildInputBuilder addPardonedLanguageString(String pardonedLanguage) {
        this.pardonedLanguageStrings.add(pardonedLanguage);
        return this;
    }


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
            new BuildInput(project, resourceChanges, includeLocations, new BuildOrder(languages), selector,
                analyze, analyzeSelector, transform, transformSelector, transformGoals, messagePrinter, throwOnErrors,
                pardonedLanguages);
        return input;
    }

    public BuildInput build(Injector injector) {
        return build(injector.getInstance(IDependencyService.class), injector.getInstance(ILanguagePathService.class));
    }
}
