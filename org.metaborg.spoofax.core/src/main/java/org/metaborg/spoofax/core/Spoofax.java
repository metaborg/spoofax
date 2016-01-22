package org.metaborg.spoofax.core;

import org.metaborg.core.MetaBorgGeneric;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Facade for instantiating and accessing the Metaborg API, instantiated with the Spoofax implementation.
 */
public class Spoofax extends MetaBorgGeneric<IStrategoTerm, IStrategoTerm, IStrategoTerm> {
    public final ITermFactoryService termFactoryService;
    public final IStrategoRuntimeService strategoRuntimeService;
    public final IStrategoCommon strategoCommon;
    

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
        super(module, loader, IStrategoTerm.class, IStrategoTerm.class, IStrategoTerm.class);
        
        this.termFactoryService = injector.getInstance(ITermFactoryService.class);
        this.strategoRuntimeService = injector.getInstance(IStrategoRuntimeService.class);
        this.strategoCommon = injector.getInstance(IStrategoCommon.class);
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
        this(module, defaultPluginLoader());
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
