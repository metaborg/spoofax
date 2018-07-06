package org.metaborg.spoofax.core.stratego.primitive.flowspec;

import static mb.nabl2.terms.build.TermBuild.B;

import java.util.List;
import java.util.Optional;

import org.spoofax.interpreter.core.InterpreterException;

import mb.flowspec.runtime.solver.FixedPoint;
import mb.flowspec.runtime.solver.ParseException;
import mb.flowspec.runtime.solver.StaticInfo;
import mb.nabl2.solver.ISolution;
import mb.nabl2.spoofax.analysis.IResult;
import mb.nabl2.spoofax.primitives.AnalysisPrimitive;
import mb.nabl2.terms.ITerm;

public class FS_solve extends AnalysisPrimitive {

    public FS_solve() {
        super(FS_solve.class.getSimpleName(), 1);
    }

    @Override protected Optional<? extends ITerm> call(IResult result, ITerm term, List<ITerm> terms)
            throws InterpreterException {
        final StaticInfo staticInfo = StaticInfo.match().match(terms.get(0))
                .orElseThrow(() -> new ParseException("Parse error on reading the transfer function file"));
        final ISolution sol = result.solution();
        final FixedPoint solver = new FixedPoint();
        final ISolution solution = solver.entryPoint(sol, staticInfo);
        return Optional.of(B.newBlob(result.withSolution(solution)));
    }

}
