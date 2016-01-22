package org.metaborg.core;

import java.lang.reflect.ParameterizedType;

import org.metaborg.core.action.IActionService;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.core.language.dialect.IDialectIdentifier;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.outline.IOutlineService;
import org.metaborg.core.plugin.IModulePluginLoader;
import org.metaborg.core.processing.analyze.IAnalysisResultProcessor;
import org.metaborg.core.processing.parse.IParseResultProcessor;
import org.metaborg.core.style.ICategorizerService;
import org.metaborg.core.style.IStylerService;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.tracing.IHoverService;
import org.metaborg.core.tracing.IResolverService;
import org.metaborg.core.tracing.ITracingService;
import org.metaborg.core.transform.ITransformService;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

/**
 * Generic version of the {@link MetaBorg} facade. Call the public methods to perform common operations, or use the
 * public final fields to access services directly.
 * 
 * This facade should only be used with a module that implements these services, like the Spoofax module, or through a
 * subclassed facade like to the Spoofax facade. Using this facade with the {@link MetaborgModule} will cause an
 * exception on construction.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 * @param <T>
 *            Type of transformed fragments.
 */
public class MetaBorgGeneric<P, A, T> extends MetaBorg {
    private final Class<P> pClass;
    private final Class<A> aClass;
    private final Class<T> tClass;

    public final IDialectService dialectService;
    public final IDialectIdentifier dialectIdentifier;

    public final ISyntaxService<P> syntaxService;
    public final IAnalysisService<P, A> analysisService;
    public final ITransformService<P, A, T> transformService;

    public final IBuilder<P, A, T> builder;

    public final IParseResultProcessor<P> parseResultProcessor;
    public final IAnalysisResultProcessor<P, A> analysisResultProcessor;

    public final IActionService actionService;
    public final IMenuService menuService;

    public final ITracingService<P, A, T> tracingService;

    public final ICategorizerService<P, A> categorizerService;
    public final IStylerService<P, A> stylerService;
    public final IHoverService<P, A> hoverService;
    public final IResolverService<P, A> resolverService;
    public final IOutlineService<P, A> outlineService;
    public final ICompletionService completionService;



    /**
     * Instantiate the generic MetaBorg API.
     * 
     * @param module
     *            MetaBorg module to use, which should implement all services in this facade. Do not use
     *            {@link MetaborgModule}.
     * @param loader
     *            Module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgGeneric(MetaborgModule module, IModulePluginLoader loader, Class<P> pClass, Class<A> aClass,
        Class<T> tClass) throws MetaborgException {
        super(module, loader);

        this.pClass = pClass;
        this.aClass = aClass;
        this.tClass = tClass;

        this.dialectService = injector.getInstance(IDialectService.class);
        this.dialectIdentifier = injector.getInstance(IDialectIdentifier.class);

        this.syntaxService = getP(new TypeLiteral<ISyntaxService<P>>() {});
        this.analysisService = getA(new TypeLiteral<IAnalysisService<P, A>>() {});
        this.transformService = getT(new TypeLiteral<ITransformService<P, A, T>>() {});

        this.builder = getT(new TypeLiteral<IBuilder<P, A, T>>() {});

        this.parseResultProcessor = getP(new TypeLiteral<IParseResultProcessor<P>>() {});
        this.analysisResultProcessor = getA(new TypeLiteral<IAnalysisResultProcessor<P, A>>() {});

        this.actionService = injector.getInstance(IActionService.class);
        this.menuService = injector.getInstance(IMenuService.class);

        this.tracingService = getT(new TypeLiteral<ITracingService<P, A, T>>() {});

        this.categorizerService = getA(new TypeLiteral<ICategorizerService<P, A>>() {});
        this.stylerService = getA(new TypeLiteral<IStylerService<P, A>>() {});
        this.hoverService = getA(new TypeLiteral<IHoverService<P, A>>() {});
        this.resolverService = getA(new TypeLiteral<IResolverService<P, A>>() {});
        this.outlineService = getA(new TypeLiteral<IOutlineService<P, A>>() {});
        this.completionService = injector.getInstance(ICompletionService.class);
    }

    /**
     * Instantiate the generic MetaBorg API.
     * 
     * @param module
     *            MetaBorg module to use, which should implement all services in this facade. Do not use
     *            {@link MetaborgModule}.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgGeneric(MetaborgModule module, Class<P> pClass, Class<A> aClass, Class<T> tClass)
        throws MetaborgException {
        this(module, defaultPluginLoader(), pClass, aClass, tClass);
    }

    /**
     * Instantiate the generic MetaBorg API.
     * 
     * @param loader
     *            Module plugin loader to use.
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgGeneric(IModulePluginLoader loader, Class<P> pClass, Class<A> aClass, Class<T> tClass)
        throws MetaborgException {
        this(defaultModule(), loader, pClass, aClass, tClass);
    }

    /**
     * Instantiate the generic MetaBorg API.
     * 
     * @throws MetaborgException
     *             When loading plugins or dependency injection fails.
     */
    public MetaBorgGeneric(Class<P> pClass, Class<A> aClass, Class<T> tClass) throws MetaborgException {
        this(defaultModule(), defaultPluginLoader(), pClass, aClass, tClass);
    }


    private <K> K getP(TypeLiteral<K> typeLiteral) {
        final Class<? super K> rawType = typeLiteral.getRawType();
        final ParameterizedType type = Types.newParameterizedType(rawType, pClass);
        @SuppressWarnings("unchecked") final Key<K> key = (Key<K>) Key.get(type);
        return injector.getInstance(key);
    }

    private <K> K getA(TypeLiteral<K> typeLiteral) {
        final Class<? super K> rawType = typeLiteral.getRawType();
        final ParameterizedType type = Types.newParameterizedType(rawType, pClass, aClass);
        @SuppressWarnings("unchecked") final Key<K> key = (Key<K>) Key.get(type);
        return injector.getInstance(key);
    }

    private <K> K getT(TypeLiteral<K> typeLiteral) {
        final Class<? super K> rawType = typeLiteral.getRawType();
        final ParameterizedType type = Types.newParameterizedType(rawType, pClass, aClass, tClass);
        @SuppressWarnings("unchecked") final Key<K> key = (Key<K>) Key.get(type);
        return injector.getInstance(key);
    }
}
