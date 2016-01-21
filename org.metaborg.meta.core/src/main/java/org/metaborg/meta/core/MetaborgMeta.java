package org.metaborg.meta.core;

import org.metaborg.core.MetaBorg;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.core.plugin.InjectorFactory;
import org.metaborg.core.plugin.ServiceModulePluginLoader;
import org.metaborg.meta.core.plugin.IServiceMetaModulePlugin;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Facade for instantiating and accessing the Metaborg meta API, as an extension of the {@link MetaBorg} API.
 */
public class MetaborgMeta {
    private final Injector injector;
    private final MetaBorg parent;


    /**
     * Instantiate the Metaborg meta API.
     * 
     * @param metaborg
     *            Metaborg API to extend.
     * @param module
     *            Metaborg meta-module to use.
     * @param loader
     *            Meta-module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaborgMeta(MetaBorg metaborg, MetaborgMetaModule module, IModulePluginLoader loader)
        throws MetaborgException {
        final Iterable<Module> modules = InjectorFactory.modules(module, loader);
        this.injector = InjectorFactory.createChild(metaborg.injector, modules);
        this.parent = metaborg;
    }

    /**
     * Instantiate the Metaborg meta API.
     * 
     * @param metaborg
     *            Metaborg API to extend.
     * @param module
     *            Metaborg meta-module to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaborgMeta(MetaBorg metaborg, MetaborgMetaModule module) throws MetaborgException {
        this(metaborg, module, defaultPluginLoader());
    }

    /**
     * Instantiate the Metaborg meta API.
     * 
     * @param metaborg
     *            Metaborg API to extend.
     * @param loader
     *            Meta-module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaborgMeta(MetaBorg metaborg, IModulePluginLoader loader) throws MetaborgException {
        this(metaborg, defaultModule(), loader);
    }

    /**
     * Instantiate the Metaborg meta API.
     * 
     * @param metaborg
     *            Metaborg API to extend.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaborgMeta(MetaBorg metaborg) throws MetaborgException {
        this(metaborg, defaultModule(), defaultPluginLoader());
    }

    protected static MetaborgMetaModule defaultModule() {
        return new MetaborgMetaModule();
    }

    protected static IModulePluginLoader defaultPluginLoader() {
        return new ServiceModulePluginLoader<>(IServiceMetaModulePlugin.class);
    }


    public Injector injector() {
        return injector;
    }

    public MetaBorg parent() {
        return parent;
    }
}
