package org.metaborg.core.build;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.dependency.INewDependencyService;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class CleanInputBuilder {

    private final ILanguageSpec languageSpec;

    private Set<ILanguageImpl> languages;
    private boolean addDependencyLanguages;

    private @Nullable FileSelector selector;


    public CleanInputBuilder(ILanguageSpec languageSpec) {
        this.languageSpec = languageSpec;
        reset();
    }


    public void reset() {
        languages = Sets.newHashSet();
        addDependencyLanguages = true;
        
        selector = null;
    }

    
    /**
     * Sets the languages to given language implementations.
     */
    public CleanInputBuilder withLanguages(Set<ILanguageImpl> languages) {
        this.languages = languages;
        return this;
    }

    /**
     * Adds given language implementations.
     */
    public CleanInputBuilder addLanguages(Iterable<? extends ILanguageImpl> languages) {
        Iterables.addAll(this.languages, languages);
        return this;
    }

    /**
     * Adds a single language implementation.
     */
    public CleanInputBuilder addLanguage(ILanguageImpl language) {
        this.languages.add(language);
        return this;
    }

    /**
     * Sets the languages from given language components.
     */
    public CleanInputBuilder withComponents(Iterable<ILanguageComponent> components) {
        withLanguages(LanguageUtils.toImpls(components));
        return this;
    }

    /**
     * Adds languages from given language components.
     */
    public CleanInputBuilder addComponents(Iterable<? extends ILanguageComponent> components) {
        addLanguages(LanguageUtils.toImpls(components));
        return this;
    }

    /**
     * Adds languages from given language component.
     */
    public CleanInputBuilder addComponent(ILanguageComponent component) {
        addLanguages(component.contributesTo());
        return this;
    }

    /**
     * Sets if compile time dependencies should be added to languages when the input is built. Defaults to true.
     */
    public CleanInputBuilder withCompileDependencyLanguages(boolean addDependencyLanguages) {
        this.addDependencyLanguages = addDependencyLanguages;
        return this;
    }


    /**
     * Sets the file selector to given selector.
     */
    public CleanInputBuilder withSelector(FileSelector selector) {
        this.selector = selector;
        return this;
    }


    /**
     * Builds a clean input object from the current state.
     * 
     * @throws MetaborgException
     *             When {@link INewDependencyService#compileDependencies(ILanguageSpec)} throws.
     */
    public CleanInput build(INewDependencyService dependencyService) throws MetaborgException {
        if(addDependencyLanguages) {
            final Iterable<ILanguageComponent> compileComponents = dependencyService.compileDependencies(languageSpec);
            final Iterable<ILanguageImpl> compileImpls = LanguageUtils.toImpls(compileComponents);
            addLanguages(compileImpls);
        }

        return new CleanInput(languageSpec, languages, selector);
    }
}
