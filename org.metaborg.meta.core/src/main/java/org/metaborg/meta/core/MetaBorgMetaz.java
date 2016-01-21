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
 * Facade for instantiating and accessing the MetaBorg meta API, as an extension of the {@link MetaBorg} API.
 */
public class MetaBorgMetaz {
    public final Injector injector;
    public final MetaBorg parent;


    /**
     * Instantiate the MetaBorg meta API.
     * 
     * @param metaborg
     *            MetaBorg API to extend.
     * @param module
     *            MetaBorg meta-module to use.
     * @param loader
     *            Meta-module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgMetaz(MetaBorg metaborg, MetaborgMetaModule module, IModulePluginLoader loader)
        throws MetaborgException {
        final Iterable<Module> modules = InjectorFactory.modules(module, loader);
        this.injector = InjectorFactory.createChild(metaborg.injector, modules);
        this.parent = metaborg;
    }

    /**
     * Instantiate the MetaBorg meta API.
     * 
     * @param metaborg
     *            MetaBorg API to extend.
     * @param module
     *            MetaBorg meta-module to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgMetaz(MetaBorg metaborg, MetaborgMetaModule module) throws MetaborgException {
        this(metaborg, module, defaultPluginLoader());
    }

    /**
     * Instantiate the MetaBorg meta API.
     * 
     * @param metaborg
     *            MetaBorg API to extend.
     * @param loader
     *            Meta-module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgMetaz(MetaBorg metaborg, IModulePluginLoader loader) throws MetaborgException {
        this(metaborg, defaultModule(), loader);
    }

    /**
     * Instantiate the MetaBorg meta API.
     * 
     * @param metaborg
     *            MetaBorg API to extend.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgMetaz(MetaBorg metaborg) throws MetaborgException {
        this(metaborg, defaultModule(), defaultPluginLoader());
    }


    protected static MetaborgMetaModule defaultModule() {
        return new MetaborgMetaModule();
    }

    protected static IModulePluginLoader defaultPluginLoader() {
        return new ServiceModulePluginLoader<>(IServiceMetaModulePlugin.class);
    }
}
