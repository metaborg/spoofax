package org
/**
 * @deprecated Use {@link NewLanguagePathService} instead.
 */.metaborg.core.build;

import com.google.common.collect.*;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.dependency.INewDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.util.resource.ResourceUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * Fluent interface for creating {@link BuildInput} objects.
 * 
 * @see BuildInput
 */
public class NewBuildInputBuilder {
    private final ILanguageSpec languageSpec;

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
    private Collection<ITransformerGoal> transformGoals;

    private @Nullable IBuildMessagePrinter messagePrinter;
    private boolean throwOnErrors;
    private Set<ILanguageImpl> pardonedLanguages;
    private Set<String> pardonedLanguageStrings;


    @Inject public NewBuildInputBuilder(ILanguageSpec languageSpec) {
        this.languageSpec = languageSpec;
        reset();
    }


    /**
     * Resets the builder to its original state.
     */
    public NewBuildInputBuilder reset() {
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
    public NewBuildInputBuilder withState(@Nullable BuildState state) {
        this.state = state;
        return this;
    }

    /**
     * Sets the languages to given languague implementations.
     */
    public NewBuildInputBuilder withLanguages(Set<ILanguageImpl> languages) {
        this.languages = languages;
        return this;
    }

    /**
     * Adds given language implementations.
     */
    public NewBuildInputBuilder addLanguages(Iterable<? extends ILanguageImpl> languages) {
        Iterables.addAll(this.languages, languages);
        return this;
    }

    /**
     * Adds a single language implementation.
     */
    public NewBuildInputBuilder addLanguage(ILanguageImpl language) {
        this.languages.add(language);
        return this;
    }

    /**
     * Sets the languages from given language components.
     */
    public NewBuildInputBuilder withComponents(Iterable<ILanguageComponent> components) {
        withLanguages(LanguageUtils.toImpls(components));
        return this;
    }

    /**
     * Adds languages from given language components.
     */
    public NewBuildInputBuilder addComponents(Iterable<? extends ILanguageComponent> components) {
        addLanguages(LanguageUtils.toImpls(components));
        return this;
    }

    /**
     * Adds languages from given language component.
     */
    public NewBuildInputBuilder addComponent(ILanguageComponent component) {
        addLanguages(component.contributesTo());
        return this;
    }

    /**
     * Sets if compile time dependencies should be added to languages when the input is built. Defaults to true.
     */
    public NewBuildInputBuilder withCompileDependencyLanguages(boolean addDependencyLanguages) {
        this.addDependencyLanguages = addDependencyLanguages;
        return this;
    }


    /**
     * Sets the source changes to given resource changes.
     */
    public NewBuildInputBuilder withSourceChanges(Collection<ResourceChange> sourceChanges) {
        this.sourceChanges = sourceChanges;
        return this;
    }

    /**
     * Adds a source change.
     */
    public NewBuildInputBuilder addSourceChanges(Iterable<ResourceChange> sourceChanges) {
        Iterables.addAll(this.sourceChanges, sourceChanges);
        return this;
    }

    /**
     * Set the source changes to additions from given sources.
     */
    public NewBuildInputBuilder withSources(Iterable<FileObject> sources) {
        this.sourceChanges = Lists.newLinkedList();
        return addSources(sources);
    }

    /**
     * Add addition source changes from given identified sources.
     */
    public NewBuildInputBuilder addIdentifiedSources(Iterable<IdentifiedResource> sources) {
        for(IdentifiedResource source : sources) {
            addSource(source);
        }
        return this;
    }

    /**
     * Add addition source changes from given sources.
     */
    public NewBuildInputBuilder addSources(Iterable<FileObject> sources) {
        for(FileObject source : sources) {
            addSource(source);
        }
        return this;
    }

    /**
     * Adds a single addition source change from given identified source.
     */
    public NewBuildInputBuilder addSource(IdentifiedResource source) {
        sourceChanges.add(new ResourceChange(source.resource));
        return this;
    }

    /**
     * Adds a single addition source change from given source.
     */
    public NewBuildInputBuilder addSource(FileObject source) {
        sourceChanges.add(new ResourceChange(source));
        return this;
    }

    /**
     * Add addition source changes from source files at given source locations.
     */
    public NewBuildInputBuilder addSourcesFromSourceLocations(Iterable<FileObject> sourceLocations) {
        addSources(ResourceUtils.expand(sourceLocations));
        return this;
    }

    /**
     * Sets if addition source changes should be added from source at default source locations, when the input is built.
     * Defaults to false.
     */
    public NewBuildInputBuilder withSourcesFromDefaultSourceLocations(boolean addSourcesFromDefaultSourceLocations) {
        this.addSourcesFromDefaultSourceLocations = addSourcesFromDefaultSourceLocations;
        return this;
    }


    /**
     * Sets the include files to given files.
     */
    public NewBuildInputBuilder withIncludePaths(Multimap<ILanguageImpl, FileObject> includePaths) {
        this.includePaths = includePaths;
        return this;
    }

    /**
     * Add given include files for given language.
     */
    public NewBuildInputBuilder addIncludePaths(ILanguageImpl language, Iterable<FileObject> includePaths) {
        this.includePaths.putAll(language, includePaths);
        return this;
    }

    /**
     * Sets if default include files should be added when the input is build. Defaults to true.
     */
    public NewBuildInputBuilder withDefaultIncludePaths(boolean addDefaultIncludePaths) {
        this.addDefaultIncludePaths = addDefaultIncludePaths;
        return this;
    }


    /**
     * Sets the file selector to given selector.
     */
    public NewBuildInputBuilder withSelector(FileSelector selector) {
        this.selector = selector;
        return this;
    }


    /**
     * Sets if analysis should be executed. Defaults to true.
     */
    public NewBuildInputBuilder withAnalysis(boolean analyze) {
        this.analyze = analyze;
        return this;
    }

    /**
     * Sets the analysis file selector to given selector.
     */
    public NewBuildInputBuilder withAnalyzeSelector(FileSelector analyzeSelector) {
        this.analyzeSelector = analyzeSelector;
        return this;
    }


    /**
     * Sets if transformations should be executed. Defaults to true.
     */
    public NewBuildInputBuilder withTransformation(boolean transform) {
        this.transform = transform;
        return this;
    }

    /**
     * Sets the transformation file selector to given selector.
     */
    public NewBuildInputBuilder withTransformSelector(FileSelector transformSelector) {
        this.transformSelector = transformSelector;
        return this;
    }

    /**
     * Sets the transform goals to given transform goals.
     */
    public NewBuildInputBuilder withTransformGoals(Collection<ITransformerGoal> transformGoals) {
        this.transformGoals = transformGoals;
        return this;
    }

    /**
     * Adds a single transform goal.
     */
    public NewBuildInputBuilder addTransformGoal(ITransformerGoal goal) {
        this.transformGoals.add(goal);
        return this;
    }


    /**
     * Sets the message printer to given message printer.
     */
    public NewBuildInputBuilder withMessagePrinter(IBuildMessagePrinter messagePrinter) {
        this.messagePrinter = messagePrinter;
        return this;
    }

    /**
     * Sets if a runtime exception should be thrown when errors occur. Defaults to false.
     */
    public NewBuildInputBuilder withThrowOnErrors(boolean throwOnErrors) {
        this.throwOnErrors = throwOnErrors;
        return this;
    }

    /**
     * Set the pardoned languages from given set of pardoned languages.
     */
    public NewBuildInputBuilder withPardonedLanguages(Set<ILanguageImpl> pardonedLanguages) {
        this.pardonedLanguages = pardonedLanguages;
        return this;
    }

    /**
     * Set the pardoned languages from given language names.
     */
    public NewBuildInputBuilder withPardonedLanguageStrings(Iterable<String> pardonedLanguages) {
        this.pardonedLanguageStrings = Sets.newHashSet(pardonedLanguages);
        return this;
    }

    /**
     * Adds a single pardoned language.
     */
    public NewBuildInputBuilder addPardonedLanguage(ILanguageImpl pardonedLanguage) {
        this.pardonedLanguages.add(pardonedLanguage);
        return this;
    }

    /**
     * Adds a single pardoned language from given language name.
     */
    public NewBuildInputBuilder addPardonedLanguageString(String pardonedLanguage) {
        this.pardonedLanguageStrings.add(pardonedLanguage);
        return this;
    }


    /**
     * Builds a build input object from the current state.
     * 
     * @throws MetaborgException
     *             When {@link INewDependencyService#compileDependencies} throws.
     */
    public BuildInput build(INewDependencyService dependencyService, ILanguagePathService languagePathService)
        throws MetaborgException {
        if(state == null) {
            state = new BuildState();
        }

        final Iterable<ILanguageComponent> compileComponents = dependencyService.compileDependencies(this.languageSpec);
        final Iterable<ILanguageImpl> compileImpls = LanguageUtils.toImpls(compileComponents);
        if(addDependencyLanguages) {
            addLanguages(compileImpls);
        }

        if(addDefaultIncludePaths) {
            for(ILanguageImpl language : compileImpls) {
                addIncludePaths(language, languagePathService.includePaths(this.languageSpec, language.belongsTo().name()));
            }
        }

        if(addSourcesFromDefaultSourceLocations) {
            for(ILanguageImpl language : compileImpls) {
                final Iterable<IdentifiedResource> sources = languagePathService.sourceFiles(this.languageSpec, language);
                addIdentifiedSources(sources);
            }
        }

        for(ILanguageImpl language : languages) {
            if(pardonedLanguageStrings.contains(language.belongsTo().name())) {
                addPardonedLanguage(language);
            }
        }

        final BuildInput input =
            new BuildInput(state, this.languageSpec, sourceChanges, includePaths, new BuildOrder(languages), selector, analyze,
                analyzeSelector, transform, transformSelector, transformGoals, messagePrinter, throwOnErrors,
                pardonedLanguages);
        return input;
    }
}
