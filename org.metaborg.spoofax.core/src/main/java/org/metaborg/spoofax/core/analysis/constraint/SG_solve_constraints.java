package org.metaborg.spoofax.core.analysis.constraint;

import static mb.nabl2.terms.build.TermBuild.B;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.processing.NullCancel;
import org.metaborg.core.processing.NullProgress;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.context.scopegraph.ISingleFileScopeGraphUnit;
import org.metaborg.spoofax.core.context.scopegraph.SingleFileScopeGraphContext;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxPrimitive;
import org.metaborg.util.functions.Function1;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.ParseError;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import mb.flowspec.runtime.solver.FixedPoint;
import mb.flowspec.runtime.solver.ParseException;
import mb.flowspec.runtime.solver.TFFileInfo;
import mb.nabl2.config.NaBL2DebugConfig;
import mb.nabl2.constraints.IConstraint;
import mb.nabl2.constraints.messages.IMessageInfo;
import mb.nabl2.solver.Fresh;
import mb.nabl2.solver.ISolution;
import mb.nabl2.solver.SolverException;
import mb.nabl2.solver.messages.IMessages;
import mb.nabl2.solver.messages.Messages;
import mb.nabl2.solver.solvers.BaseSolver.GraphSolution;
import mb.nabl2.solver.solvers.ImmutableBaseSolution;
import mb.nabl2.solver.solvers.SingleFileSolver;
import mb.nabl2.spoofax.analysis.Actions;
import mb.nabl2.spoofax.analysis.CustomSolution;
import mb.nabl2.spoofax.analysis.FinalResult;
import mb.nabl2.spoofax.analysis.InitialResult;
import mb.nabl2.spoofax.analysis.UnitResult;
import mb.nabl2.stratego.ConstraintTerms;
import mb.nabl2.stratego.StrategoTerms;
import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.unification.PersistentUnifier;

/**
 * Do a one-shot constraint solver using some code duplicated & adapted from
 * {@link ConstraintSingleFileAnalyzer#analyzeAll(java.util.Map, Set, org.metaborg.spoofax.core.context.scopegraph.ISingleFileScopeGraphContext, org.strategoxt.HybridInterpreter, String, IProgress, ICancel)}
 * This does not take custom analysis into account. Since it's not connected to
 * the editor, messages are returned. Expects the AST as current term. Expects
 * the InitialResult and UnitResult as term arguments. Returns a 3-tuple with
 * the analysed AST, the analysis result term (blob), and the messages 3-tuple
 * (errors, warnings, notes). Warnings are 2-tuples of the origin term and the
 * message (messages can be trees, see {@link #buildMessages(Set)})
 */
public class SG_solve_constraints extends ASpoofaxPrimitive {
    public static final ILogger logger = LoggerUtils.logger(SG_solve_constraints.class);

    protected final IResourceService resourceService;

    public @Inject SG_solve_constraints(final IResourceService resourceService) {
        super(SG_solve_constraints.class.getSimpleName(), 1, 0);
        this.resourceService = resourceService;
    }

    private Optional<? extends ITerm> call(IContext context, ITermFactory factory, ITerm ast,
            Function<ITerm, Optional<ITerm>> doAction) {
        String source = context.location().getPublicURIString();
        ICancel cancel = new NullCancel();
        IProgress progress = new NullProgress();
        final ISingleFileScopeGraphUnit unit = new SolveConstraintsPrimitiveUnit(source);

        try {
            // initial
            InitialResult initialResult;
            final Optional<ITerm> customInitial;
            {
                ITerm initialResultTerm = doAction.apply(Actions.analyzeInitial(source, ast))
                        .orElseThrow(() -> new AnalysisException(context, "No initial result."));
                initialResult = InitialResult.matcher().match(initialResultTerm, PersistentUnifier.Immutable.of())
                        .orElseThrow(() -> new MetaborgException("Invalid initial results."));
                customInitial = doAction.apply(Actions.customInitial(source, ast));
                initialResult = initialResult.withCustomResult(customInitial);
            }

            // unit
            UnitResult unitResult;
            final Optional<ITerm> customUnit;
            {
                final ITerm unitResultTerm = doAction.apply(Actions.analyzeUnit(source, ast, initialResult.getArgs()))
                        .orElseThrow(() -> new AnalysisException(context, "No unit result."));
                unitResult = UnitResult.matcher().match(unitResultTerm, PersistentUnifier.Immutable.of())
                        .orElseThrow(() -> new MetaborgException("Invalid unit results."));
                final ITerm desugaredAST = unitResult.getAST();
                customUnit = doAction
                        .apply(Actions.customUnit(source, desugaredAST, customInitial.orElse(B.EMPTY_TUPLE)));
                unitResult = unitResult.withCustomResult(customUnit);
                unit.setUnitResult(unitResult);
            }

            // solve
            ISolution solution;
            {
                Set<IConstraint> constraints = Sets.union(initialResult.getConstraints(), unitResult.getConstraints());
                Function1<String, String> fresh = unit.fresh()::fresh;
                final IProgress subprogress = progress.subProgress(1);
                // Note how we do not support debugging and external calls
                final SingleFileSolver solver = new SingleFileSolver(new NaBL2DebugConfig(NaBL2DebugConfig.Flag.ALL),
                        (name, args) -> Optional.empty());
                GraphSolution preSolution = solver.solveGraph(ImmutableBaseSolution.of(initialResult.getConfig(),
                        constraints, PersistentUnifier.Immutable.of()), fresh, cancel, subprogress);
                preSolution = solver.reportUnsolvedGraphConstraints(preSolution);
                solution = solver.solve(preSolution, fresh, cancel, subprogress);
                solution = solver.reportUnsolvedConstraints(solution);
                if (!solution.flowSpecSolution().controlFlowGraph().isEmpty()) {
                    solution = new FixedPoint().entryPoint(solution,
                            getFlowSpecTransferFunctions(context.language(), factory));
                }
                unit.setSolution(solution);
            }

            // final
            FinalResult finalResult;
            final Optional<ITerm> customFinal;
            {
                ITerm finalResultTerm = doAction.apply(Actions.analyzeFinal(source))
                        .orElseThrow(() -> new AnalysisException(context, "No final result."));
                finalResult = FinalResult.matcher().match(finalResultTerm, PersistentUnifier.Immutable.of())
                        .orElseThrow(() -> new MetaborgException("Invalid final results."));
                customFinal = doAction.apply(Actions.customFinal(source, customInitial.orElse(B.EMPTY_TUPLE),
                        customUnit.map(cu -> Collections.singletonList(cu)).orElse(Collections.emptyList())));
                finalResult = finalResult.withCustomResult(customFinal);
                unit.setFinalResult(finalResult);
            }

            Optional<CustomSolution> customSolution = customFinal
                    .flatMap(cf -> CustomSolution.matcher().match(cf, PersistentUnifier.Immutable.of()));
            customSolution.ifPresent(cs -> unit.setCustomSolution(cs));

            Messages.Transient messageBuilder = Messages.Transient.of();
            messageBuilder.addAll(Messages.unsolvedErrors(solution.constraints()));
            messageBuilder.addAll(solution.messages().getAll());
            IMessages messages = messageBuilder.freeze();

            return Optional
                    .of(B.newTuple(unitResult.getAST(), B.newBlob(unit), B.newTuple(buildMessages(messages.getErrors()),
                            buildMessages(messages.getWarnings()), buildMessages(messages.getNotes()))));
        } catch (MetaborgException | SolverException e) {
            logger.warn("Analysis of failed.", e);
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.debug("Analysis was interrupted.");
            return Optional.empty();
        }
    }

    private IListTerm buildMessages(Set<IMessageInfo> set) {
        return B.newList(set.stream()
                .map(messageinfo -> B.newTuple(messageinfo.getOriginTerm(), messageinfo.getContent().build()))
                .toArray(l -> new ITerm[l]));
    }

    protected TFFileInfo getFlowSpecTransferFunctions(ILanguageImpl impl, ITermFactory factory) {
        Optional<TFFileInfo> result = Optional.empty();
        for (ILanguageComponent comp : impl.components()) {
            Optional<TFFileInfo> tfs = getFlowSpecTransferFunctions(comp, factory);
            if (tfs.isPresent()) {
                if (!result.isPresent()) {
                    result = tfs;
                } else {
                    result = Optional.of(result.get().addAll(tfs.get()));
                }
            }
        }
        if (!result.isPresent()) {
            return TFFileInfo.of();
        }
        return result.get();
    }

    private Optional<TFFileInfo> getFlowSpecTransferFunctions(ILanguageComponent component, ITermFactory termFactory) {
        TFFileInfo transferFunctions;
        StrategoTerms strategoTerms = new StrategoTerms(termFactory);
        FileObject tfs = resourceService.resolve(component.location(),
                AbstractConstraintAnalyzer.TRANSFER_FUNCTIONS_FILE);
        try {
            IStrategoTerm sTerm = termFactory
                    .parseFromString(IOUtils.toString(tfs.getContent().getInputStream(), StandardCharsets.UTF_8));
            ITerm term = strategoTerms.fromStratego(sTerm);
            transferFunctions = TFFileInfo.match().match(term, PersistentUnifier.Immutable.of())
                    .orElseThrow(() -> new ParseException("Parse error on reading the transfer function file"));
        } catch (ParseError | ParseException | IOException e) {
            return Optional.empty();
        }
        return Optional.of(transferFunctions);
    }

    @Override
    protected @Nullable IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, org.spoofax.interpreter.core.IContext context) throws MetaborgException, IOException {
        StrategoTerms strategoTerms = new StrategoTerms(factory);
        ITerm ast = ConstraintTerms.specialize(strategoTerms.fromStratego(current));
        Function<ITerm, Optional<ITerm>> strategy = (term) -> {
            context.setCurrent(strategoTerms.toStratego(ConstraintTerms.explicate(term)));
            try {
                if (svars[0].evaluate(context)) {
                    return Optional.ofNullable(context.current()).map(strategoTerms::fromStratego);
                } else {
                    return Optional.empty();
                }
            } catch (InterpreterException e) {
                return Optional.empty();
            }
        };
        Optional<? extends ITerm> result = call(metaborgContext(context), factory, ast, strategy);
        return result.map(ConstraintTerms::explicate).map(strategoTerms::toStratego).orElse(null);
    }

    /**
     * Based on {@link SingleFileScopeGraphContext#Unit}
     */
    private static final class SolveConstraintsPrimitiveUnit implements ISingleFileScopeGraphUnit {
        private static final long serialVersionUID = 1375323492952578547L;
        private final String resource;
        private final Fresh fresh;
        private InitialResult initialResult;
        private UnitResult unitResult;
        private ISolution solution;
        private CustomSolution customSolution;
        private FinalResult finalResult;

        private SolveConstraintsPrimitiveUnit(String resource) {
            this.resource = resource;
            this.fresh = new Fresh();
            clear();
        }

        private SolveConstraintsPrimitiveUnit(String resource, ISolution solution, UnitResult unitResult,
                InitialResult initialResult, FinalResult finalResult) {
            this(resource);
            this.initialResult = initialResult;
            this.unitResult = unitResult;
            this.finalResult = finalResult;
            this.solution = solution;
        }

        @Override
        public Optional<ISolution> solution() {
            return Optional.ofNullable(solution);
        }

        @Override
        public String resource() {
            return resource;
        }

        @Override
        public boolean isPrimary() {
            return true;
        }

        @Override
        public Fresh fresh() {
            return fresh;
        }

        @Override
        public Optional<CustomSolution> customSolution() {
            return Optional.ofNullable(customSolution);
        }

        @Override
        public Set<IConstraint> constraints() {
            final Set<IConstraint> constraints = Sets.newHashSet();
            initialResult().ifPresent(ir -> constraints.addAll(ir.getConstraints()));
            unitResult().ifPresent(ur -> constraints.addAll(ur.getConstraints()));
            return constraints;
        }

        @Override
        public Optional<UnitResult> unitResult() {
            return Optional.ofNullable(unitResult);
        }

        @Override
        public void setUnitResult(UnitResult result) {
            this.unitResult = result;
        }

        @Override
        public void setSolution(ISolution solution) {
            this.solution = solution;
        }

        @Override
        public void setInitialResult(InitialResult result) {
            this.initialResult = result;
        }

        @Override
        public void setFinalResult(FinalResult result) {
            this.finalResult = result;
        }

        @Override
        public void setCustomSolution(CustomSolution solution) {
            this.customSolution = solution;
        }

        @Override
        public Optional<InitialResult> initialResult() {
            return Optional.ofNullable(initialResult);
        }

        @Override
        public Optional<FinalResult> finalResult() {
            return Optional.ofNullable(finalResult);
        }

        @Override
        public void clear() {
            this.initialResult = null;
            this.unitResult = null;
            this.solution = null;
            this.customSolution = null;
            this.finalResult = null;
            this.fresh.reset();
        }
    }
}