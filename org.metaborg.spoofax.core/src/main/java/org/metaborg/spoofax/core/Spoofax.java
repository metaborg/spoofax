package org.metaborg.spoofax.core;

import org.metaborg.core.MetaBorgGeneric;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.build.ISpoofaxBuilder;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Module;
import com.google.inject.util.Types;

/**
 * Facade for instantiating and accessing the Metaborg API, instantiated with the Spoofax implementation.
 */
@SuppressWarnings("hiding")
public class Spoofax extends
    MetaBorgGeneric<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>, IStrategoTerm> {
    public final ISpoofaxUnitService unitService;
    public final ISpoofaxSyntaxService syntaxService;
    public final ISpoofaxAnalysisService analysisService;
    public final ISpoofaxTransformService transformService;
    public final ISpoofaxBuilder builder;
    public final ISpoofaxProcessorRunner processorRunner;

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
        this.unitService = injector.getInstance(ISpoofaxUnitService.class);
        this.syntaxService = injector.getInstance(ISpoofaxSyntaxService.class);
        this.analysisService = injector.getInstance(ISpoofaxAnalysisService.class);
        this.transformService = injector.getInstance(ISpoofaxTransformService.class);
        this.builder = injector.getInstance(ISpoofaxBuilder.class);
        this.processorRunner = injector.getInstance(ISpoofaxProcessorRunner.class);

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
