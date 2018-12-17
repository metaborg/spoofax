package org.metaborg.spoofax.core.stratego.primitive.flowspec;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.ParseError;

import com.google.inject.Inject;

import mb.flowspec.runtime.interpreter.InterpreterBuilder;
import mb.flowspec.runtime.solver.FixedPoint;
import mb.flowspec.runtime.solver.ParseException;
import mb.nabl2.solver.ISolution;
import mb.nabl2.spoofax.analysis.IResult;
import mb.nabl2.spoofax.primitives.AnalysisPrimitive;
import mb.nabl2.stratego.StrategoTerms;
import mb.nabl2.terms.ITerm;

public class FS_solve extends AbstractPrimitive implements ILanguageCache {
    private static final ILogger logger = LoggerUtils.logger(FS_solve.class);
    protected final IResourceService resourceService;
    protected final ITermFactory termFactory;
    protected final StrategoTerms strategoTerms;
    private final AnalysisPrimitive prim;
    private ILanguageImpl currentLanguage;

    private static final String FLOWSPEC_STATIC_INFO_DIR = "target/metaborg/flowspec-static-info";
    private final Map<ILanguageComponent, InterpreterBuilder> flowSpecTransferFunctionCache = new HashMap<>();

    @Inject public FS_solve(IResourceService resourceService, ITermFactoryService termFactoryService) {
        super(FS_solve.class.getSimpleName(), 0, 2);
        this.resourceService = resourceService;
        this.termFactory = termFactoryService.getGeneric();
        this.strategoTerms = new StrategoTerms(termFactory);
        prim = new AnalysisPrimitive(FS_solve.class.getSimpleName(), 1) {
            @Override protected Optional<? extends ITerm> call(IResult result, ITerm term, List<ITerm> terms)
                    throws InterpreterException {
                final Optional<List<String>> propertyNames = M.listElems(M.stringValue()).match(terms.get(0));
                final ISolution sol = result.solution();
                final FixedPoint solver = new FixedPoint();
                final InterpreterBuilder interpBuilder = getFlowSpecInterpreterBuilder(currentLanguage);
                if (propertyNames.isPresent()) {
                        final ISolution solution = solver.entryPoint(sol, interpBuilder, propertyNames.get());
                        return Optional.of(B.newBlob(result.withSolution(solution)));
                }
                return Optional.empty();
            }
        };
    }

    @Override public boolean call(org.spoofax.interpreter.core.IContext env, Strategy[] svars, IStrategoTerm[] tvars)
            throws InterpreterException {
        this.currentLanguage = ((IContext) env.contextObject()).language();
        return prim.call(env, svars, tvars);
    }

    protected Optional<InterpreterBuilder> getFlowSpecInterpreterBuilder(ILanguageComponent component) {
        Optional<InterpreterBuilder> optInterpB = Optional.ofNullable(flowSpecTransferFunctionCache.get(component));
        if (optInterpB.isPresent()) {
            return optInterpB;
        }

        optInterpB = getFlowSpecInterpreterBuilder(component, resourceService, termFactory, strategoTerms);

        if (!optInterpB.isPresent()) {
            return optInterpB;
        }

        logger.debug("Caching FlowSpec static info for language {}", component);
        flowSpecTransferFunctionCache.put(component, optInterpB.get());
        return optInterpB;
    }

    public InterpreterBuilder getFlowSpecInterpreterBuilder(ILanguageImpl impl) {
        return getFlowSpecInterpreterBuilder(impl, this::getFlowSpecInterpreterBuilder);
    }

    public static InterpreterBuilder getFlowSpecInterpreterBuilder(ILanguageImpl impl, Function<ILanguageComponent, Optional<InterpreterBuilder>> getStaticInfo) {
        Optional<InterpreterBuilder> result = Optional.empty();
        for (ILanguageComponent comp : impl.components()) {
            Optional<InterpreterBuilder> optInterpB = getStaticInfo.apply(comp);
            if (optInterpB.isPresent()) {
                logger.debug("Found FlowSpec static info directory for {}.", comp);
                if (!result.isPresent()) {
                    result = optInterpB;
                } else {
                    result = Optional.of(result.get().add(optInterpB.get()));
                }
            }
        }
        if (!result.isPresent()) {
            logger.error("No FlowSpec static info found for {}", impl);
            return new InterpreterBuilder();
        }
        return result.get();
    }

    public static Optional<InterpreterBuilder> getFlowSpecInterpreterBuilder(ILanguageComponent component,
            IResourceService resourceService, ITermFactory termFactory, StrategoTerms strategoTerms) {
        FileObject staticInfoDir = resourceService.resolve(component.location(), FLOWSPEC_STATIC_INFO_DIR);
        try {
            InterpreterBuilder result = new InterpreterBuilder();
            for(FileObject staticInfoFile : staticInfoDir.getChildren()) {
                try {
                    String moduleName = FilenameUtils.removeExtension(staticInfoFile.getName().getBaseName().replace('+', '/'));
                    IStrategoTerm sTerm = termFactory
                            .parseFromString(IOUtils.toString(staticInfoFile.getContent().getInputStream(), StandardCharsets.UTF_8));
                    ITerm term = strategoTerms.fromStratego(sTerm);
                    result.add(term, moduleName);
                } catch (IOException e) {
                    logger.info("Could not read FlowSpec static info file for {}. \n{}", component, e.getMessage());
                } catch (ParseError | ParseException e) {
                    logger.warn("Could not parse FlowSpec static info file for {}. \nError: {}", component, e.getMessage());
                }
            }
            return Optional.of(result);
        } catch(FileSystemException e) {
            logger.info("Could not find FlowSpec static info directory for {}.", component);
            return Optional.empty();
        }
    }

    @Override
    public void invalidateCache(ILanguageComponent component) {
        logger.debug("Removing cached flowspec transfer functions for {}", component);
        flowSpecTransferFunctionCache.remove(component);
    }

    @Override
    public void invalidateCache(ILanguageImpl impl) {
        logger.debug("Removing cached flowspec transfer functions for {}", impl);
        for(ILanguageComponent component : impl.components()) {
            flowSpecTransferFunctionCache.remove(component);
        }
    }

}
