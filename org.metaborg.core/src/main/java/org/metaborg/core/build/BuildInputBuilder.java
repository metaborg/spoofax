package org.metaborg.core.build;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.messages.IMessagePrinter;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.util.resource.ResourceUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Fluent interface for creating {@link BuildInput} objects.
 * 
 * @see BuildInput
 */
public class BuildInputBuilder {
    private final IProject project;

    private BuildState state;

    private Set<ILanguageImpl> languages;
    private boolean addDependencyLanguages;

    private Multimap<ILanguageImpl, FileObject> includePaths;
    private boolean addDefaultIncludePaths;

    private Collection<ResourceChange> sourceChanges;
    private boolean addSourcesFromDefaultSourceLocations;

    private @Nullable FileSelector selector;

    private boolean analyze;
    private @Nullable FileSelector analyzeSelector;

    private boolean transform;
    private @Nullable FileSelector transformSelector;
    private Collection<ITransformGoal> transformGoals;

    private @Nullable IMessagePrinter messagePrinter;
    private boolean throwOnErrors;
    private Set<ILanguageImpl> pardonedLanguages;
    private Set<String> pardonedLanguageStrings;


    @Inject public BuildInputBuilder(IProject project) {
        this.project = project;
        reset();
    }


    /**
     * Resets the builder to its original state.
     */
    public BuildInputBuilder reset() {
        state = null;
        languages = Sets.newHashSet();
        addDependencyLanguages = true;
        includePaths = HashMultimap.create();
        addDefaultIncludePaths = true;
        sourceChanges = Lists.newLinkedList();
        addSourcesFromDefaultSourceLocations = false;
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
     * Sets the build state to given build state.
     */
    public BuildInputBuilder withState(@Nullable BuildState state) {
        this.state = state;
        return this;
    }

    /**
     * Sets the languages to given language implementations.
     */
    public BuildInputBuilder withLanguages(Set<ILanguageImpl> languages) {
        this.languages = languages;
        return this;
    }

    /**
     * Adds given language implementations.
     */
    public BuildInputBuilder addLanguages(Iterable<? extends ILanguageImpl> languages) {
        Iterables.addAll(this.languages, languages);
        return this;
    }

    /**
     * Adds a single language implementation.
     */
    public BuildInputBuilder addLanguage(ILanguageImpl language) {
        this.languages.add(language);
        return this;
    }

    /**
     * Sets the languages from given language components.
     */
    public BuildInputBuilder withComponents(Iterable<ILanguageComponent> components) {
        withLanguages(LanguageUtils.toImpls(components));
        return this;
    }

    /**
     * Adds languages from given language components.
     */
    public BuildInputBuilder addComponents(Iterable<? extends ILanguageComponent> components) {
        addLanguages(LanguageUtils.toImpls(components));
        return this;
    }

    /**
     * Adds languages from given language component.
     */
    public BuildInputBuilder addComponent(ILanguageComponent component) {
        addLanguages(component.contributesTo());
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
     * Sets the source changes to given resource changes.
     */
    public BuildInputBuilder withSourceChanges(Collection<ResourceChange> sourceChanges) {
        this.sourceChanges = sourceChanges;
        return this;
    }

    /**
     * Adds a source change.
     */
    public BuildInputBuilder addSourceChanges(Iterable<ResourceChange> sourceChanges) {
        Iterables.addAll(this.sourceChanges, sourceChanges);
        return this;
    }

    /**
     * Set the source changes to additions from given sources.
     */
    public BuildInputBuilder withSources(Iterable<FileObject> sources) {
        this.sourceChanges = Lists.newLinkedList();
        return addSources(sources);
    }

    /**
     * Add addition source changes from given identified sources.
     */
    public BuildInputBuilder addIdentifiedSources(Iterable<IdentifiedResource> sources) {
        for(IdentifiedResource source : sources) {
            addSource(source);
        }
        return this;
    }

    /**
     * Add addition source changes from given sources.
     */
    public BuildInputBuilder addSources(Iterable<FileObject> sources) {
        for(FileObject source : sources) {
            addSource(source);
        }
        return this;
    }

    /**
     * Adds a single addition source change from given identified source.
     */
    public BuildInputBuilder addSource(IdentifiedResource source) {
        sourceChanges.add(new ResourceChange(source.resource));
        return this;
    }

    /**
     * Adds a single addition source change from given source.
     */
    public BuildInputBuilder addSource(FileObject source) {
        sourceChanges.add(new ResourceChange(source));
        return this;
    }

    /**
     * Add addition source changes from source files at given source locations.
     */
    public BuildInputBuilder addSourcesFromSourceLocations(Iterable<FileObject> sourceLocations) {
        addSources(ResourceUtils.expand(sourceLocations));
        return this;
    }

    /**
     * Sets if addition source changes should be added from source at default source locations, when the input is built.
     * Defaults to false.
     */
    public BuildInputBuilder withSourcesFromDefaultSourceLocations(boolean addSourcesFromDefaultSourceLocations) {
        this.addSourcesFromDefaultSourceLocations = addSourcesFromDefaultSourceLocations;
        return this;
    }


    /**
     * Sets the include files to given files.
     */
    public BuildInputBuilder withIncludePaths(Multimap<ILanguageImpl, FileObject> includePaths) {
        this.includePaths = includePaths;
        return this;
    }

    /**
     * Add given include files for given language.
     */
    public BuildInputBuilder addIncludePaths(ILanguageImpl language, Iterable<FileObject> includePaths) {
        this.includePaths.putAll(language, includePaths);
        return this;
    }

    /**
     * Sets if default include files should be added when the input is build. Defaults to true.
     */
    public BuildInputBuilder withDefaultIncludePaths(boolean addDefaultIncludePaths) {
        this.addDefaultIncludePaths = addDefaultIncludePaths;
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
    public BuildInputBuilder withTransformGoals(Collection<ITransformGoal> transformGoals) {
        this.transformGoals = transformGoals;
        return this;
    }

    /**
     * Adds a single transform goal.
     */
    public BuildInputBuilder addTransformGoal(ITransformGoal goal) {
        this.transformGoals.add(goal);
        return this;
    }


    /**
     * Sets the message printer to given message printer.
     */
    public BuildInputBuilder withMessagePrinter(IMessagePrinter messagePrinter) {
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
    public BuildInputBuilder withPardonedLanguages(Set<ILanguageImpl> pardonedLanguages) {
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
    public BuildInputBuilder addPardonedLanguage(ILanguageImpl pardonedLanguage) {
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
     * 
     * @throws MetaborgException
     *             When {@link IDependencyService#compileDeps} throws.
     */
    public BuildInput build(IDependencyService dependencyService, ILanguagePathService languagePathService)
        throws MetaborgException {
        if(state == null) {
            state = new BuildState();
        }

        if(addDependencyLanguages) {
            final Iterable<ILanguageComponent> compileComponents = dependencyService.compileDeps(this.project);
            final Iterable<ILanguageImpl> compileImpls = LanguageUtils.toImpls(compileComponents);
            addLanguages(compileImpls);
        }

        if(addDefaultIncludePaths) {
            for(ILanguageImpl language : languages) {
                addIncludePaths(language, languagePathService.includePaths(this.project, language.belongsTo().name()));
            }
        }

        if(addSourcesFromDefaultSourceLocations) {
            for(ILanguageImpl language : languages) {
                final Iterable<IdentifiedResource> sources = languagePathService.sourceFiles(this.project, language);
                addIdentifiedSources(sources);
            }
        }

        for(ILanguageImpl language : languages) {
            if(pardonedLanguageStrings.contains(language.belongsTo().name())) {
                addPardonedLanguage(language);
            }
        }

        return new BuildInput(state, this.project, sourceChanges, includePaths, new BuildOrder(languages), selector,
            analyze, analyzeSelector, transform, transformSelector, transformGoals, messagePrinter, throwOnErrors,
            pardonedLanguages);
    }
}
