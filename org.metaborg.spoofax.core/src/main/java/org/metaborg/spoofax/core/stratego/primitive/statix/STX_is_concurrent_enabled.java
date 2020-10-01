package org.metaborg.spoofax.core.stratego.primitive.statix;

import java.io.IOException;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class STX_is_concurrent_enabled extends ASpoofaxContextPrimitive {

    public STX_is_concurrent_enabled() {
        super(STX_is_concurrent_enabled.class.getSimpleName(), 0, 0);
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) throws MetaborgException, IOException {
        boolean statixConcurrent = context.language().components().stream()
                .anyMatch(lc -> lc.hasFacet(AnalysisFacet.class) && lc.config().statixConcurrent());
        if(statixConcurrent) {
            return current;
        }
        return null;
    }

}