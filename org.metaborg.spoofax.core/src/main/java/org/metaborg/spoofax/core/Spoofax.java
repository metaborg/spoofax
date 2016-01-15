package org.metaborg.spoofax.core;

import org.metaborg.core.Metaborg;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;

/**
 * Facade for instantiating and accessing the Metaborg API, instantiated with the Spoofax implementation.
 */
public class Spoofax extends Metaborg {
    /**
     * Instantiate the Metaborg API with a Spoofax implementation.
     * 
     * @param module
     *            Spoofax module to use.
     * @param loader
     *            Module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Spoofax(SpoofaxModule module, IModulePluginLoader loader) throws MetaborgException {
        super(module, loader);
    }

    /**
     * Instantiate the Metaborg API with a Spoofax implementation.
     * 
     * @param module
     *            Spoofax module to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Spoofax(SpoofaxModule module) throws MetaborgException {
        super(module);
    }

    /**
     * Instantiate the Metaborg API with a Spoofax implementation.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Spoofax(IModulePluginLoader loader) throws MetaborgException {
        this(defaultModule(), loader);
    }

    /**
     * Instantiate the Metaborg API with a Spoofax implementation.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Spoofax() throws MetaborgException {
        this(defaultModule());
    }

    protected static SpoofaxModule defaultModule() {
        return new SpoofaxModule();
    }
}
