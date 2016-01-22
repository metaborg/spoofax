package org.metaborg.core;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.paths.INewLanguagePathService;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.*;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.core.plugin.IServiceModulePlugin;
import org.metaborg.core.plugin.InjectorFactory;
import org.metaborg.core.plugin.ServiceModulePluginLoader;
import org.metaborg.core.project.ILanguageSpecService;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.project.configuration.ILanguageSpecConfigService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Facade for instantiating and accessing the MetaBorg API. Call the public methods to perform common operations, or use
 * the public final fields to access services directly. All services and operations available in this facade are
 * implemented when using the default {@link MetaborgModule}.
 */
public class MetaBorg {
    public final Injector injector;

    public final IResourceService resourceService;

    public final ILanguageService languageService;
    public final INewLanguageDiscoveryService languageDiscoveryService;
    public final ILanguageIdentifierService languageIdentifierService;
    public final INewLanguagePathService languagePathService;

    public final IContextService contextService;

    public final IProjectService projectService;
    public final ILanguageSpecService languageSpecService;
    public final ILanguageSpecConfigService languageSpecConfigService;

    public final ISourceTextService sourceTextService;

    public final IEditorRegistry editorRegistry;


    /**
     * Instantiate the MetaBorg API.
     * 
     * @param module
     *            MetaBorg module to use.
     * @param loader
     *            Module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorg(MetaborgModule module, IModulePluginLoader loader) throws MetaborgException {
        final Iterable<Module> modules = InjectorFactory.modules(module, loader);
        this.injector = InjectorFactory.create(modules);

        this.resourceService = injector.getInstance(IResourceService.class);
        this.languageService = injector.getInstance(ILanguageService.class);
        this.languageDiscoveryService = injector.getInstance(INewLanguageDiscoveryService.class);
        this.languageIdentifierService = injector.getInstance(ILanguageIdentifierService.class);
        this.languagePathService = injector.getInstance(INewLanguagePathService.class);

        this.contextService = injector.getInstance(IContextService.class);

        this.projectService = injector.getInstance(IProjectService.class);
        this.languageSpecService = injector.getInstance(ILanguageSpecService.class);
        this.languageSpecConfigService = injector.getInstance(ILanguageSpecConfigService.class);

        this.sourceTextService = injector.getInstance(ISourceTextService.class);

        this.editorRegistry = injector.getInstance(IEditorRegistry.class);
    }

    /**
     * Instantiate the MetaBorg API.
     * 
     * @param module
     *            MetaBorg module to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorg(MetaborgModule module) throws MetaborgException {
        this(module, defaultPluginLoader());
    }

    /**
     * Instantiate the MetaBorg API.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorg(IModulePluginLoader loader) throws MetaborgException {
        this(defaultModule(), loader);
    }

    /**
     * Instantiate the MetaBorg API.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorg() throws MetaborgException {
        this(defaultModule(), defaultPluginLoader());
    }


    /**
     * @see INewLanguageDiscoveryService#request(FileObject)
     * @see INewLanguageDiscoveryService#discover(INewLanguageDiscoveryRequest)
     */
    public Iterable<ILanguageComponent> discoverLanguages(FileObject location) throws MetaborgException {
        return languageDiscoveryService.discover(languageDiscoveryService.request(location));
    }

    /**
     * @see ILanguageIdentifierService#identify(FileObject, ILanguageImpl)
     */
    public boolean identifyResource(FileObject resource, ILanguageImpl language) {
        return languageIdentifierService.identify(resource, language);
    }

    /**
     * @see ILanguageIdentifierService#identify(FileObject)
     */
    public @Nullable ILanguageImpl identifyResource(FileObject resource) {
        return languageIdentifierService.identify(resource);
    }


    protected static MetaborgModule defaultModule() {
        return new MetaborgModule();
    }

    protected static IModulePluginLoader defaultPluginLoader() {
        return new ServiceModulePluginLoader<>(IServiceModulePlugin.class);
    }
}
