package org.metaborg.spoofax.meta.core;

import org.metaborg.core.MetaBorg;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.meta.core.MetaBorgMeta;
import org.metaborg.spoofax.core.Spoofax;

/**
 * Facade for instantiating and accessing the MetaBorg meta API, as an extension of the {@link MetaBorg} API,
 * instantiated with the Spoofax implementation.
 */
public class SpoofaxMeta extends MetaBorgMeta {
    @SuppressWarnings("hiding") public final Spoofax parent;

    public final SpoofaxMetaBuilder metaBuilder;


    /**
     * Instantiate the MetaBorg meta API, with a Spoofax implementation.
     * 
     * @param spoofax
     *            MetaBorg API, implemented by Spoofax, to extend.
     * @param module
     *            Spoofax meta-module to use.
     * @param loader
     *            Meta-module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public SpoofaxMeta(Spoofax spoofax, SpoofaxMetaModule module, IModulePluginLoader loader) throws MetaborgException {
        super(spoofax, module, loader);
        this.parent = spoofax;

        this.metaBuilder = injector.getInstance(SpoofaxMetaBuilder.class);
    }

    /**
     * Instantiate the MetaBorg meta API, with a Spoofax implementation.
     * 
     * @param spoofax
     *            MetaBorg API, implemented by Spoofax, to extend.
     * @param module
     *            Spoofax meta-module to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public SpoofaxMeta(Spoofax spoofax, SpoofaxMetaModule module) throws MetaborgException {
        this(spoofax, module, defaultPluginLoader());
    }

    /**
     * Instantiate the MetaBorg meta API, with a Spoofax implementation.
     * 
     * @param spoofax
     *            MetaBorg API, implemented by Spoofax, to extend.
     * @param loader
     *            Meta-module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public SpoofaxMeta(Spoofax spoofax, IModulePluginLoader loader) throws MetaborgException {
        this(spoofax, defaultModule(), loader);
    }

    /**
     * Instantiate the MetaBorg meta API, with a Spoofax implementation.
     * 
     * @param spoofax
     *            MetaBorg API, implemented by Spoofax, to extend.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public SpoofaxMeta(Spoofax spoofax) throws MetaborgException {
        this(spoofax, defaultModule(), defaultPluginLoader());
    }


    protected static SpoofaxMetaModule defaultModule() {
        return new SpoofaxMetaModule();
    }
}
