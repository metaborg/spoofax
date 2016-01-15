package org.metaborg.core.build.dependency;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpecService;
import org.metaborg.core.project.IProject;

import com.google.inject.Inject;

/**
 * Forwards the implementation to the new dependency service.
 *
 * @deprecated This class is only used for the configuration system migration.
 */
@Deprecated
public class LegacyDependencyService implements IDependencyService {

    private final INewDependencyService newDependencyService;
    private final ILanguageSpecService languageSpecService;

    @Inject
    public LegacyDependencyService(final INewDependencyService newDependencyService, final ILanguageSpecService languageSpecService) {
        this.newDependencyService = newDependencyService;
        this.languageSpecService = languageSpecService;
    }

    @Override
    public Iterable<ILanguageComponent> compileDependencies(IProject project) throws MetaborgException {
        return this.newDependencyService.compileDependencies(this.languageSpecService.get(project));
    }

    @Override
    public Iterable<ILanguageComponent> runtimeDependencies(IProject project) throws MetaborgException {
        return this.newDependencyService.runtimeDependencies(this.languageSpecService.get(project));
    }

    @Override
    public MissingDependencies checkDependencies(IProject project) {
        return this.newDependencyService.checkDependencies(this.languageSpecService.get(project));
    }

    @Override
    public Iterable<ILanguageComponent> compileDependencies(ILanguageComponent component) throws MetaborgException {
        return this.newDependencyService.compileDependencies(component);
    }

    @Override
    public Iterable<ILanguageComponent> runtimeDependencies(ILanguageComponent component) throws MetaborgException {
        return this.newDependencyService.runtimeDependencies(component);
    }
}
