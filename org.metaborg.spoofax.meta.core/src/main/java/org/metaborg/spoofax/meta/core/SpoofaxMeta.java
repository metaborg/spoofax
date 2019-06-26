package org.metaborg.spoofax.meta.core;

import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.stratego.build.StrIncrModule;

import com.google.inject.Module;
import org.metaborg.core.MetaBorg;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.meta.core.MetaBorgMeta;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.build.LanguageSpecBuilder;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigBuilder;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigService;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import java.util.Arrays;

/**
 * Facade for instantiating and accessing the MetaBorg meta API, as an extension of the {@link MetaBorg} API,
 * instantiated with the Spoofax implementation.
 */
public class SpoofaxMeta extends MetaBorgMeta {
    @SuppressWarnings("hiding") public final Spoofax parent;

    public final LanguageSpecBuilder metaBuilder;

    @SuppressWarnings("hiding") public final ISpoofaxLanguageSpecService languageSpecService;
    @SuppressWarnings("hiding") public final ISpoofaxLanguageSpecConfigService languageSpecConfigService;


    /**
     * Instantiate the MetaBorg meta API, with a Spoofax implementation.
     * 
     * @param spoofax
     *            MetaBorg API, implemented by Spoofax, to extend.
     * @param loader
     *            Meta-module plugin loader to use.
     * @param module
     *            Spoofax meta-module to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public SpoofaxMeta(Spoofax spoofax, IModulePluginLoader loader, SpoofaxMetaModule module,
        Module... additionalModules) throws MetaborgException {
        super(spoofax, loader, module, addPieModules(additionalModules));
        this.parent = spoofax;

        this.languageSpecService = injector.getInstance(ISpoofaxLanguageSpecService.class);
        this.languageSpecConfigService = injector.getInstance(ISpoofaxLanguageSpecConfigService.class);

        this.metaBuilder = injector.getInstance(LanguageSpecBuilder.class);
    }

    private static Module[] addPieModules(Module[] additionalModules) {
        final Module[] result = Arrays.copyOf(additionalModules, additionalModules.length + 2);
        result[result.length - 2] = new StrIncrModule();
        result[result.length - 1] = new GuiceTaskDefsModule();
        return result;
    }

    /**
     * Instantiate the MetaBorg meta API, with a Spoofax implementation.
     * 
     * @param spoofax
     *            MetaBorg API, implemented by Spoofax, to extend.
     * @param module
     *            Spoofax meta-module to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public SpoofaxMeta(Spoofax spoofax, SpoofaxMetaModule module, Module... additionalModules)
        throws MetaborgException {
        this(spoofax, defaultPluginLoader(), module, additionalModules);
    }

    /**
     * Instantiate the MetaBorg meta API, with a Spoofax implementation.
     * 
     * @param spoofax
     *            MetaBorg API, implemented by Spoofax, to extend.
     * @param loader
     *            Meta-module plugin loader to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public SpoofaxMeta(Spoofax spoofax, IModulePluginLoader loader, Module... additionalModules)
        throws MetaborgException {
        this(spoofax, loader, defaultModule(), additionalModules);
    }

    /**
     * Instantiate the MetaBorg meta API, with a Spoofax implementation.
     * 
     * @param spoofax
     *            MetaBorg API, implemented by Spoofax, to extend.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public SpoofaxMeta(Spoofax spoofax, Module... additionalModules) throws MetaborgException {
        this(spoofax, defaultPluginLoader(), defaultModule(), additionalModules);
    }


    /**
     * @return Fresh language specification configuration builder.
     */
    public ISpoofaxLanguageSpecConfigBuilder languageSpecConfigBuilder() {
        return injector.getInstance(ISpoofaxLanguageSpecConfigBuilder.class);
    }


    protected static SpoofaxMetaModule defaultModule() {
        return new SpoofaxMetaModule();
    }
}
