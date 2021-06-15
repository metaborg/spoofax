package org.metaborg.spoofax.core.stratego.primitive.statix;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfig;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

import mb.statix.spoofax.SolverMode;

public class STX_is_concurrent_enabled extends ASpoofaxContextPrimitive {

    final ISpoofaxProjectConfigService projectConfigService;

    @Inject public STX_is_concurrent_enabled(ISpoofaxProjectConfigService projectConfigService) {
        super(STX_is_concurrent_enabled.class.getSimpleName(), 0, 0);
        this.projectConfigService = projectConfigService;
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, @SuppressWarnings("unused") Strategy[] svars,
            @SuppressWarnings("unused") IStrategoTerm[] tvars, @SuppressWarnings("unused") ITermFactory factory,
            IContext context) throws MetaborgException, IOException {
        ISpoofaxProjectConfig config = projectConfigService.get(context.project());
        if(config != null) {
            String languageName = context.language().belongsTo().name();
            Map<String, SolverMode> modes = config.statixConfig().languageModes(Collections.emptyMap());
            if(modes.containsKey(languageName) && modes.get(languageName) != SolverMode.TRADITIONAL) {
                return current;
            }
        }
        boolean concurrentInLanguage = context.language().components().stream()
                .anyMatch(lc -> lc.hasFacet(AnalysisFacet.class) && lc.config().statixConcurrentComponent());
        if(concurrentInLanguage) {
            return current;
        }
        return null;
    }

}