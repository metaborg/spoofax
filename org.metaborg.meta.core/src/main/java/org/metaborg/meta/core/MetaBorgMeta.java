package org.metaborg.meta.core;

import java.util.Collection;
import java.util.Set;

import org.metaborg.core.MetaBorg;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.core.plugin.InjectorFactory;
import org.metaborg.core.plugin.ServiceModulePluginLoader;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;
import org.metaborg.meta.core.plugin.IServiceMetaModulePlugin;
import org.metaborg.meta.core.project.ILanguageSpecService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Types;

/**
 * Facade for instantiating and accessing the MetaBorg meta API, as an extension of the {@link MetaBorg} API.
 */
public class MetaBorgMeta implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(MetaBorgMeta.class);

    public final Injector injector;
    public final MetaBorg parent;

    public final Set<AutoCloseable> autoCloseables;

    public final ILanguageSpecService languageSpecService;
    public final ILanguageSpecConfigService languageSpecConfigService;



    /**
     * Instantiate the MetaBorg meta API.
     * 
     * @param metaborg
     *            MetaBorg API to extend.
     * @param loader
     *            Meta-module plugin loader to use.
     * @param module
     *            MetaBorg meta-module to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    @SuppressWarnings("unchecked") public MetaBorgMeta(MetaBorg metaborg, IModulePluginLoader loader,
        MetaborgMetaModule module, Module... additionalModules) throws MetaborgException {
        final Collection<Module> metaModules = Lists.newArrayList(additionalModules);
        metaModules.add(module);
        final Iterable<Module> modules = InjectorFactory.modules(loader, metaModules);
        this.injector = InjectorFactory.createChild(metaborg.injector, modules);
        this.parent = metaborg;

        this.autoCloseables =
            (Set<AutoCloseable>) injector.getInstance(Key.get(Types.setOf(AutoCloseable.class), Meta.class));

        this.languageSpecService = injector.getInstance(ILanguageSpecService.class);
        this.languageSpecConfigService = injector.getInstance(ILanguageSpecConfigService.class);
    }

    /**
     * Instantiate the MetaBorg meta API.
     * 
     * @param metaborg
     *            MetaBorg API to extend.
     * @param module
     *            MetaBorg meta-module to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgMeta(MetaBorg metaborg, MetaborgMetaModule module, Module... additionalModules)
        throws MetaborgException {
        this(metaborg, defaultPluginLoader(), module, additionalModules);
    }

    /**
     * Instantiate the MetaBorg meta API.
     * 
     * @param metaborg
     *            MetaBorg API to extend.
     * @param loader
     *            Meta-module plugin loader to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgMeta(MetaBorg metaborg, IModulePluginLoader loader, Module... additionalModules)
        throws MetaborgException {
        this(metaborg, loader, defaultModule(), additionalModules);
    }

    /**
     * Instantiate the MetaBorg meta API.
     * 
     * @param metaborg
     *            MetaBorg API to extend.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgMeta(MetaBorg metaborg, Module... additionalModules) throws MetaborgException {
        this(metaborg, defaultPluginLoader(), defaultModule(), additionalModules);
    }


    /**
     * Closes the MetaBorg meta API, closing any resources and services created by the API. The parent MetaBorg
     * (non-meta) API is NOT closed.
     */
    @Override public void close() {
        logger.debug("Closing the MetaBorg meta API");
        for(AutoCloseable autoCloseable : autoCloseables) {
            try {
                autoCloseable.close();
            } catch(Exception e) {
                logger.error("Error while closing {}", e, autoCloseable);
            }
        }
    }


    protected static MetaborgMetaModule defaultModule() {
        return new MetaborgMetaModule();
    }

    protected static IModulePluginLoader defaultPluginLoader() {
        return new ServiceModulePluginLoader<>(IServiceMetaModulePlugin.class);
    }
}
