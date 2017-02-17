package org.metaborg.core;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageComponentFactory;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.core.plugin.IServiceModulePlugin;
import org.metaborg.core.plugin.InjectorFactory;
import org.metaborg.core.plugin.ServiceModulePluginLoader;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Types;

/**
 * Facade for instantiating and accessing the MetaBorg API. Call the public methods to perform common operations, or use
 * the public final fields to access services directly. All services and operations available in this facade are
 * implemented when using the default {@link MetaborgModule}.
 */
public class MetaBorg implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(MetaBorg.class);

    public final Injector injector;

    public final Set<AutoCloseable> autoCloseables;

    public final IResourceService resourceService;

    public final ILanguageService languageService;
    public final ILanguageComponentFactory languageComponentFactory;
    public final ILanguageDiscoveryService languageDiscoveryService;
    public final ILanguageIdentifierService languageIdentifierService;
    public final ILanguagePathService languagePathService;
    public final IDialectProcessor dialectProcessor;

    public final IContextService contextService;

    public final IDependencyService dependencyService;
    public final IProjectService projectService;

    public final ISourceTextService sourceTextService;

    public final IEditorRegistry editorRegistry;


    /**
     * Instantiate the MetaBorg API.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @param module
     *            MetaBorg module to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    @SuppressWarnings("unchecked") public MetaBorg(IModulePluginLoader loader, MetaborgModule module,
        Module... additionalModules) throws MetaborgException {
        final Collection<Module> metaborgModules = Lists.newArrayList(additionalModules);
        metaborgModules.add(module);
        final Iterable<Module> modules = InjectorFactory.modules(loader, metaborgModules);
        this.injector = InjectorFactory.create(modules);

        this.autoCloseables = (Set<AutoCloseable>) injector.getInstance(Key.get(Types.setOf(AutoCloseable.class)));

        this.resourceService = injector.getInstance(IResourceService.class);
        this.languageService = injector.getInstance(ILanguageService.class);
        this.languageComponentFactory = injector.getInstance(ILanguageComponentFactory.class);
        this.languageDiscoveryService = injector.getInstance(ILanguageDiscoveryService.class);
        this.languageIdentifierService = injector.getInstance(ILanguageIdentifierService.class);
        this.languagePathService = injector.getInstance(ILanguagePathService.class);
        this.dialectProcessor = injector.getInstance(IDialectProcessor.class);

        this.contextService = injector.getInstance(IContextService.class);

        this.dependencyService = injector.getInstance(IDependencyService.class);
        this.projectService = injector.getInstance(IProjectService.class);

        this.sourceTextService = injector.getInstance(ISourceTextService.class);

        this.editorRegistry = injector.getInstance(IEditorRegistry.class);
    }

    /**
     * Instantiate the MetaBorg API.
     * 
     * @param module
     *            MetaBorg module to use.
     * @param additionalModules
     *            Additional modules to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorg(MetaborgModule module, Module... additionalModules) throws MetaborgException {
        this(defaultPluginLoader(), module, additionalModules);
    }

    /**
     * Instantiate the MetaBorg API.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @param additionalModules
     *            Additional modules to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorg(IModulePluginLoader loader, Module... additionalModules) throws MetaborgException {
        this(loader, defaultModule(), additionalModules);
    }

    /**
     * Instantiate the MetaBorg API.
     * 
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorg(Module... additionalModules) throws MetaborgException {
        this(defaultPluginLoader(), defaultModule(), additionalModules);
    }

    /**
     * Closes the MetaBorg API, closing any resources and services created by the API.
     */
    @Override public void close() {
        logger.debug("Closing the MetaBorg API");
        for(AutoCloseable autoCloseable : autoCloseables) {
            try {
                autoCloseable.close();
            } catch(Exception e) {
                logger.error("Error while closing {}", e, autoCloseable);
            }
        }
    }


    /**
     * @see IResourceService#resolve(String)
     */
    public FileObject resolve(String uri) {
        return resourceService.resolve(uri);
    }

    /**
     * @see IResourceService#resolve(File)
     */
    public FileObject resolve(File file) {
        return resourceService.resolve(file);
    }

    /**
     * @see IResourceService#resolve(URI)
     */
    public FileObject resolve(URI uri) {
        return resourceService.resolve(uri);
    }


    /**
     * @see ILanguageDiscoveryService#scanLanguagesInDirectory(FileObject)
     */
    public Set<ILanguageImpl> scanLanguagesInDirectory(FileObject directory) throws MetaborgException {
        return languageDiscoveryService.scanLanguagesInDirectory(directory);
    }
    
    /**
     * @see ILanguageDiscoveryService#request(FileObject)
     * @see ILanguageDiscoveryService#discover(ILanguageDiscoveryRequest)
     * @deprecated Use {@link #scanLanguagesInDirectory(FileObject)}
     */
    @Deprecated public Iterable<ILanguageComponent> discoverLanguages(FileObject location) throws MetaborgException {
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
