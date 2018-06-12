package org.metaborg.spoofax.core.stratego.primitive.flowspec;

import static mb.nabl2.terms.build.TermBuild.B;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.constraint.AbstractConstraintAnalyzer;
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

import mb.flowspec.runtime.solver.FixedPoint;
import mb.flowspec.runtime.solver.ParseException;
import mb.flowspec.runtime.solver.StaticInfo;
import mb.nabl2.solver.ISolution;
import mb.nabl2.spoofax.analysis.IScopeGraphUnit;
import mb.nabl2.spoofax.analysis.ImmutableScopeGraphUnit;
import mb.nabl2.spoofax.primitives.AnalysisPrimitive;
import mb.nabl2.stratego.StrategoTerms;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.unification.PersistentUnifier;

public class FS_solve extends AbstractPrimitive {
    private static final ILogger logger = LoggerUtils.logger(FS_solve.class);
    protected final IResourceService resourceService;
    protected final ITermFactory termFactory;
    protected final StrategoTerms strategoTerms;
    private final AnalysisPrimitive prim;
    private IContext env;

    @Inject public FS_solve(IResourceService resourceService, ITermFactoryService termFactoryService) {
        super(FS_solve.class.getSimpleName(), 0, 1);
        this.resourceService = resourceService;
        this.termFactory = termFactoryService.getGeneric();
        this.strategoTerms = new StrategoTerms(termFactory);
        prim = new AnalysisPrimitive(FS_solve.class.getSimpleName()) {
            @Override protected Optional<? extends ITerm> call(IScopeGraphUnit unit, ITerm term, List<ITerm> terms)
                    throws InterpreterException {
                Optional<ISolution> solution = unit.solution().map(sol -> {
                    FixedPoint solver = new FixedPoint();
                    return solver.entryPoint(sol, getStaticInfo(env.language()));
                });
                return Optional.of(B.newBlob(ImmutableScopeGraphUnit.builder().from(unit).solution(solution).build()));
            }
        };
    }
    
    public boolean call(org.spoofax.interpreter.core.IContext env, Strategy[] svars, IStrategoTerm[] tvars)
            throws InterpreterException {
        this.env = (IContext) env.contextObject();
        return prim.call(env, svars, tvars);
    }
    
    protected Optional<StaticInfo> getStaticInfo(ILanguageComponent component) {
        FileObject tfs = resourceService.resolve(component.location(), AbstractConstraintAnalyzer.FLOWSPEC_STATIC_INFO_FILE);
        try {
            IStrategoTerm sTerm = termFactory
                    .parseFromString(IOUtils.toString(tfs.getContent().getInputStream(), StandardCharsets.UTF_8));
            ITerm term = strategoTerms.fromStratego(sTerm);
            return Optional.of(StaticInfo.match()
                    .match(term, PersistentUnifier.Immutable.of())
                    .orElseThrow(() -> new ParseException("Parse error on reading the transfer function file")));
        } catch (ParseError | ParseException | IOException e) {
            logger.error("Could not read FlowSpec static info file for {}. \nError: {}", component, e.getMessage());
            return Optional.empty();
        }
    }

    protected StaticInfo getStaticInfo(ILanguageImpl impl) {
        Optional<StaticInfo> result = Optional.empty();
        for (ILanguageComponent comp : impl.components()) {
            Optional<StaticInfo> sfi = getStaticInfo(comp);
            if (sfi.isPresent()) {
                if (!result.isPresent()) {
                    result = sfi;
                } else {
                    result = Optional.of(result.get().addAll(sfi.get()));
                }
            }
        }
        if (!result.isPresent()) {
            logger.error("No FlowSpec static info found for {}", impl);
            return StaticInfo.of();
        }
        return result.get();
    }

}
