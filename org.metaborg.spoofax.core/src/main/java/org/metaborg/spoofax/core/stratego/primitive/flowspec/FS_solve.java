package org.metaborg.spoofax.core.stratego.primitive.flowspec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.M;


import mb.flowspec.controlflow.IFlowSpecSolution;
import mb.flowspec.primitives.AnalysisPrimitive;
import mb.flowspec.runtime.interpreter.InterpreterBuilder;
import mb.flowspec.runtime.solver.FixedPoint;
import mb.flowspec.runtime.solver.ParseException;
import mb.flowspec.terms.B;
import mb.nabl2.spoofax.analysis.IResult;

public class FS_solve extends AbstractPrimitive implements ILanguageCache {
    private static final ILogger logger = LoggerUtils.logger(FS_solve.class);
    protected final IResourceService resourceService;
    protected final ITermFactory termFactory;
    private final AnalysisPrimitive prim;
    private ILanguageImpl currentLanguage;

    private static final String FLOWSPEC_STATIC_INFO_DIR = "target/metaborg/flowspec-static-info";
    private final Map<ILanguageComponent, InterpreterBuilder> flowSpecTransferFunctionCache = new HashMap<>();

    @jakarta.inject.Inject @javax.inject.Inject public FS_solve(IResourceService resourceService, ITermFactory termFactory) {
        super(FS_solve.class.getSimpleName(), 0, 2);
        this.resourceService = resourceService;
        this.termFactory = termFactory;
        prim = new AnalysisPrimitive(FS_solve.class.getSimpleName(), 1) {
            @Override protected Optional<? extends IStrategoTerm> call(IResult result, IStrategoTerm term, List<IStrategoTerm> terms)
                    throws InterpreterException {
                final Optional<List<String>> propertyNames = M.maybe(() -> {
                    IStrategoList list = M.list(terms.get(0));
                    ArrayList<String> propNames = new ArrayList<>(list.getSubtermCount());
                    for(IStrategoTerm stringTerm : list) {
                        propNames.add(M.string(stringTerm));
                    }
                    return propNames;
                });
                final Optional<IFlowSpecSolution> sol = AnalysisPrimitive.getFSSolution(result);
                final FixedPoint solver = new FixedPoint();
                final InterpreterBuilder interpBuilder = getFlowSpecInterpreterBuilder(currentLanguage);
                if (propertyNames.isPresent() && sol.isPresent()) {
                        final IFlowSpecSolution solution = solver.entryPoint(getFactory(), sol.get(), interpBuilder, propertyNames.get());
                        return Optional.of(B.blob(result.withCustomAnalysis(solution)));
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

        optInterpB = getFlowSpecInterpreterBuilder(component, resourceService, termFactory);

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
            IResourceService resourceService, ITermFactory termFactory) {
        FileObject staticInfoDir = resourceService.resolve(component.location(), FLOWSPEC_STATIC_INFO_DIR);
        try {
            InterpreterBuilder result = new InterpreterBuilder();
            for(FileObject staticInfoFile : staticInfoDir.getChildren()) {
                try {
                    String moduleName = FilenameUtils.removeExtension(staticInfoFile.getName().getBaseName().replace('+', '/'));
                    IStrategoTerm term = new TermReader(termFactory)
                            .parseFromStream(staticInfoFile.getContent().getInputStream());
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
