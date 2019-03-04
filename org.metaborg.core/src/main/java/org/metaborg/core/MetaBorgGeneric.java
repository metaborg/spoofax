package org.metaborg.core;

import java.lang.reflect.Type;

import org.metaborg.core.action.IActionService;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.core.language.dialect.IDialectIdentifier;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.outline.IOutlineService;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.core.processing.IProcessorRunner;
import org.metaborg.core.processing.analyze.IAnalysisResultProcessor;
import org.metaborg.core.processing.analyze.IAnalysisResultRequester;
import org.metaborg.core.processing.parse.IParseResultProcessor;
import org.metaborg.core.style.ICategorizerService;
import org.metaborg.core.style.IStylerService;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.tracing.IHoverService;
import org.metaborg.core.tracing.IResolverService;
import org.metaborg.core.tracing.ITracingService;
import org.metaborg.core.transform.ITransformService;
import org.metaborg.core.transform.ITransformUnit;
import org.metaborg.core.unit.IUnitService;
import org.metaborg.util.inject.GenericInjectUtils;

import com.google.inject.Module;
import com.google.inject.TypeLiteral;

/**
 * Generic version of the {@link MetaBorg} facade. Call the public methods to perform common operations, or use the
 * public final fields to access services directly.
 * 
 * This facade should only be used with a module that implements these services, like the Spoofax module, or through a
 * subclassed facade like to the Spoofax facade. Using this facade with the {@link MetaborgModule} will cause an
 * exception on construction.
 * 
 * @param <I>
 *            Type of input units.
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 * @param <AU>
 *            Type of analyze unit updates.
 * @param <TU>
 *            Type of transform units with any input.
 * @param <TUP>
 *            Type of transform units with parse units as input.
 * @param <TUA>
 *            Type of transform units with analyze units as input.
 * @param <F>
 *            Type of fragments.
 */
public class MetaBorgGeneric<I extends IInputUnit, P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, TU extends ITransformUnit<?>, TUP extends ITransformUnit<P>, TUA extends ITransformUnit<A>, TA extends ITransformAction, F>
    extends MetaBorg {
    public final IDialectService dialectService;
    public final IDialectIdentifier dialectIdentifier;

    public final IUnitService<I, P, A, AU, TUP, TUA, TA> unitService;

    public final ISyntaxService<I, P> syntaxService;
    public final IAnalysisService<P, A, AU> analysisService;
    public final ITransformService<P, A, TUP, TUA, TA> transformService;

    public final IBuilder<P, A, AU, TU> builder;
    public final IProcessorRunner<P, A, AU, TU> processorRunner;

    public final IParseResultProcessor<I, P> parseResultProcessor;
    public final IAnalysisResultProcessor<I, P, A> analysisResultProcessor;
    public final IAnalysisResultRequester<I, A> analysisResultRequester;

    public final IActionService<TA> actionService;
    public final IMenuService menuService;

    public final ITracingService<P, A, TU, F> tracingService;

    public final ICategorizerService<P, A, F> categorizerService;
    public final IStylerService<F> stylerService;
    public final IHoverService<P, A> hoverService;
    public final IResolverService<P, A> resolverService;
    public final IOutlineService<P, A> outlineService;
    public final ICompletionService<P> completionService;



    /**
     * Instantiate the generic MetaBorg API.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @param module
     *            MetaBorg module to use, which should implement all services in this facade. Do not use
     *            {@link MetaborgModule}.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgGeneric(Class<I> iClass, Class<P> pClass, Class<A> aClass, Class<AU> auClass, Type tClass,
        Type tupClass, Type tuaClass, Type taClass, Class<F> fClass, IModulePluginLoader loader, MetaborgModule module,
        Module... additionalModules) throws MetaborgException {
        super(loader, module, additionalModules);

        this.dialectService = injector.getInstance(IDialectService.class);
        this.dialectIdentifier = injector.getInstance(IDialectIdentifier.class);

        this.unitService = instance(new TypeLiteral<IUnitService<I, P, A, AU, TUP, TUA, TA>>() {}, iClass, pClass, aClass,
            auClass, tupClass, tuaClass, taClass);

        this.syntaxService = instance(new TypeLiteral<ISyntaxService<I, P>>() {}, iClass, pClass);
        this.analysisService = instance(new TypeLiteral<IAnalysisService<P, A, AU>>() {}, pClass, aClass, auClass);
        this.transformService =
            instance(new TypeLiteral<ITransformService<P, A, TUP, TUA, TA>>() {}, pClass, aClass, tupClass, tuaClass, taClass);

        this.builder = instance(new TypeLiteral<IBuilder<P, A, AU, TU>>() {}, pClass, aClass, auClass, tClass);
        this.processorRunner =
            instance(new TypeLiteral<IProcessorRunner<P, A, AU, TU>>() {}, pClass, aClass, auClass, tClass);

        this.parseResultProcessor = instance(new TypeLiteral<IParseResultProcessor<I, P>>() {}, iClass, pClass);
        this.analysisResultProcessor =
            instance(new TypeLiteral<IAnalysisResultProcessor<I, P, A>>() {}, iClass, pClass, aClass);
        this.analysisResultRequester =
            instance(new TypeLiteral<IAnalysisResultRequester<I, A>>() {}, iClass, aClass);

        this.actionService = instance(new TypeLiteral<IActionService<TA>>(){}, taClass);
        this.menuService = injector.getInstance(IMenuService.class);

        this.tracingService =
            instance(new TypeLiteral<ITracingService<P, A, TU, F>>() {}, pClass, aClass, tClass, fClass);

        this.categorizerService = instance(new TypeLiteral<ICategorizerService<P, A, F>>() {}, pClass, aClass, fClass);
        this.stylerService = instance(new TypeLiteral<IStylerService<F>>() {}, fClass);
        this.hoverService = instance(new TypeLiteral<IHoverService<P, A>>() {}, pClass, aClass);
        this.resolverService = instance(new TypeLiteral<IResolverService<P, A>>() {}, pClass, aClass);
        this.outlineService = instance(new TypeLiteral<IOutlineService<P, A>>() {}, pClass, aClass);
        this.completionService = instance(new TypeLiteral<ICompletionService<P>>() {}, pClass);
    }

    /**
     * Instantiate the generic MetaBorg API.
     * 
     * @param module
     *            MetaBorg module to use, which should implement all services in this facade. Do not use
     *            {@link MetaborgModule}.
     * @param additionalModules
     *            Additional modules to use.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgGeneric(Class<I> iClass, Class<P> pClass, Class<A> aClass, Class<AU> auClass, Class<TU> tClass,
        Class<TUP> tupClass, Class<TUA> tuaClass, Class<TA> taClass, Class<F> fClass, MetaborgModule module, Module... additionalModules)
        throws MetaborgException {
        this(iClass, pClass, aClass, auClass, tClass, tupClass, tuaClass, taClass, fClass, defaultPluginLoader(), module,
            additionalModules);
    }


    private <K> K instance(TypeLiteral<K> typeLiteral, Type... typeArgs) {
        return GenericInjectUtils.<K>instance(injector, typeLiteral, typeArgs);
    }
}
