package org.metaborg.spoofax.core.stratego.primitive.flowspec;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

import java.util.List;
import java.util.Optional;

import org.metaborg.core.context.IContext;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.constraint.AbstractConstraintAnalyzer;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

import mb.flowspec.runtime.solver.FixedPoint;
import mb.flowspec.runtime.solver.StaticInfo;
import mb.nabl2.solver.ISolution;
import mb.nabl2.spoofax.analysis.IScopeGraphUnit;
import mb.nabl2.spoofax.analysis.ImmutableScopeGraphUnit;
import mb.nabl2.spoofax.primitives.AnalysisPrimitive;
import mb.nabl2.stratego.StrategoTerms;
import mb.nabl2.terms.ITerm;

public class FS_solve extends AbstractPrimitive {
    protected final IResourceService resourceService;
    protected final ITermFactory termFactory;
    protected final StrategoTerms strategoTerms;
    private final AnalysisPrimitive prim;
    private IContext env;

    @Inject public FS_solve(IResourceService resourceService, ITermFactoryService termFactoryService) {
        super(FS_solve.class.getSimpleName(), 0, 2);
        this.resourceService = resourceService;
        this.termFactory = termFactoryService.getGeneric();
        this.strategoTerms = new StrategoTerms(termFactory);
        prim = new AnalysisPrimitive(FS_solve.class.getSimpleName(), 1) {
            @Override protected Optional<? extends ITerm> call(IScopeGraphUnit unit, ITerm currentTerm, List<ITerm> terms)
                    throws InterpreterException {
                Optional<ISolution> solution = unit.solution().flatMap(sol -> M.listElems(M.stringValue()).match(terms.get(0)).map(l -> {
                    FixedPoint solver = new FixedPoint();
                    final StaticInfo flowSpecStaticInfo = AbstractConstraintAnalyzer.getFlowSpecStaticInfo(env.language(),
                        comp -> AbstractConstraintAnalyzer.getFlowSpecStaticInfo(
                                comp, resourceService, termFactory, strategoTerms));
                    return solver.entryPoint(sol, flowSpecStaticInfo, l);
                }));
                return Optional.of(B.newBlob(ImmutableScopeGraphUnit.builder().from(unit).solution(solution).build()));
            }
        };
    }

    public boolean call(org.spoofax.interpreter.core.IContext env, Strategy[] svars, IStrategoTerm[] tvars)
            throws InterpreterException {
        this.env = (IContext) env.contextObject();
        return prim.call(env, svars, tvars);
    }

}
