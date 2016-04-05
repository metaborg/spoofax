package org.metaborg.spoofax.core;

import org.metaborg.core.MetaBorgGeneric;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Module;
import com.google.inject.util.Types;

/**
 * Facade for instantiating and accessing the Metaborg API, instantiated with the Spoofax implementation.
 */
public class Spoofax extends
    MetaBorgGeneric<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>, IStrategoTerm> {
    public final ITermFactoryService termFactoryService;
    public final IStrategoRuntimeService strategoRuntimeService;
    public final IStrategoCommon strategoCommon;


    /**
     * Instantiate the MetaBorg API with a Spoofax implementation.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @param module
     *            Spoofax module to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Spoofax(IModulePluginLoader loader, SpoofaxModule module, Module... additionalModules)
        throws MetaborgException {
        super(ISpoofaxInputUnit.class, ISpoofaxParseUnit.class, ISpoofaxAnalyzeUnit.class,
            ISpoofaxAnalyzeUnitUpdate.class,
            Types.newParameterizedType(ISpoofaxTransformUnit.class, Types.subtypeOf(Object.class)),
            Types.newParameterizedType(ISpoofaxTransformUnit.class, ISpoofaxParseUnit.class),
            Types.newParameterizedType(ISpoofaxTransformUnit.class, ISpoofaxAnalyzeUnit.class), IStrategoTerm.class,
            loader, module, additionalModules);

        this.termFactoryService = injector.getInstance(ITermFactoryService.class);
        this.strategoRuntimeService = injector.getInstance(IStrategoRuntimeService.class);
        this.strategoCommon = injector.getInstance(IStrategoCommon.class);
    }

    /**
     * Instantiate the MetaBorg API with a Spoofax implementation.
     * 
     * @param module
     *            Spoofax module to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Spoofax(SpoofaxModule module, Module... additionalModules) throws MetaborgException {
        this(defaultPluginLoader(), module, additionalModules);
    }

    /**
     * Instantiate the MetaBorg API with a Spoofax implementation.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Spoofax(IModulePluginLoader loader, Module... additionalModules) throws MetaborgException {
        this(loader, defaultModule(), additionalModules);
    }

    /**
     * Instantiate the MetaBorg API with a Spoofax implementation.
     * 
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public Spoofax(Module... additionalModules) throws MetaborgException {
        this(defaultPluginLoader(), defaultModule(), additionalModules);
    }


    protected static SpoofaxModule defaultModule() {
        return new SpoofaxModule();
    }
}
