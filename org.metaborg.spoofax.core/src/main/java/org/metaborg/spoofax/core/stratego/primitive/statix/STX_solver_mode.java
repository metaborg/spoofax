package org.metaborg.spoofax.core.stratego.primitive.statix;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.StatixSolverMode;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfig;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

import mb.flowspec.terms.B;
import mb.statix.spoofax.SolverMode;

public class STX_solver_mode extends ASpoofaxContextPrimitive {

    final ISpoofaxProjectConfigService projectConfigService;

    @Inject public STX_solver_mode(ISpoofaxProjectConfigService projectConfigService) {
        super(STX_solver_mode.class.getSimpleName(), 0, 0);
        this.projectConfigService = projectConfigService;
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) throws MetaborgException, IOException {
        ISpoofaxProjectConfig config = projectConfigService.get(context.project());
        if(config != null) {
            String languageName = context.language().belongsTo().name();
            Map<String, SolverMode> modes = config.statixConfig().languageModes(Collections.emptyMap());
            if(modes.containsKey(languageName)) {
                return B.blob(modes.get(languageName));
            }
        }
        // @formatter:off
        return B.blob(getSolverMode(context.language().components().stream()
                .filter(lc -> lc.hasFacet(AnalysisFacet.class) && lc.config().statixSolverMode() != null)
                .findAny()
                .map(lc -> lc.config().statixSolverMode())
                .orElse(StatixSolverMode.traditional)));
        // @formatter:on
    }

    private SolverMode getSolverMode(StatixSolverMode mode) throws MetaborgException {
        switch(mode) {
            case traditional:
                return SolverMode.TRADITIONAL;
            case concurrent:
                return SolverMode.CONCURRENT;
            case incrementalScopeGraphDiff:
                return SolverMode.INCREMENTAL_SCOPEGRAPH_DIFF;
            default:
                throw new MetaborgException("Cannot get solver mode for configuration option " + mode);
        }
    }

}
