package org.metaborg.core;

import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.core.plugin.IServiceModulePlugin;
import org.metaborg.core.plugin.InjectorFactory;
import org.metaborg.core.plugin.ServiceModulePluginLoader;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Facade for instantiating and accessing the Metaborg API.
 */
public class Metaborg {
    private final Injector injector;


    /**
     * Instantiate the Metaborg API.
     * 
     * @param module
     *            Metaborg module to use.
     * @param loader
     *            Module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Metaborg(MetaborgModule module, IModulePluginLoader loader) throws MetaborgException {
        final Iterable<Module> modules = InjectorFactory.modules(module, loader);
        this.injector = InjectorFactory.create(modules);
    }

    /**
     * Instantiate the Metaborg API.
     * 
     * @param module
     *            Metaborg module to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Metaborg(MetaborgModule module) throws MetaborgException {
        this(module, defaultPluginLoader());
    }
    
    /**
     * Instantiate the Metaborg API.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Metaborg(IModulePluginLoader loader) throws MetaborgException {
        this(defaultModule(), loader);
    }

    /**
     * Instantiate the Metaborg API.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Metaborg() throws MetaborgException {
        this(defaultModule(), defaultPluginLoader());
    }
    
    protected static MetaborgModule defaultModule() {
        return new MetaborgModule();
    }
    
    protected static IModulePluginLoader defaultPluginLoader() {
        return new ServiceModulePluginLoader<>(IServiceModulePlugin.class);
    }
    
    
    public Injector injector() {
        return injector;
    }
}
