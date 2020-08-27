package org.metaborg.spoofax.core.outline;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

public class OutlineFacetFromESV {
    public static @Nullable OutlineFacet create(IStrategoAppl esv) {
        final IStrategoAppl outlineTerm = ESVReader.findTerm(esv, "OutlineView");
        if(outlineTerm == null) {
            return null;
        }
        final String strategyName = ESVReader.termContents(outlineTerm.getSubterm(0));

        final int expandTo;
        final IStrategoAppl expandToTerm = ESVReader.findTerm(esv, "ExpandToLevel");
        if(expandToTerm == null) {
            expandTo = 0;
        } else {
            final IStrategoTerm expandToNumberTerm = expandToTerm.getSubterm(0);
            if(TermUtils.isInt(expandToNumberTerm)) {
                expandTo = TermUtils.toJavaInt(expandToNumberTerm);
            } else if(TermUtils.isString(expandToNumberTerm)) {
                expandTo = Integer.parseInt(TermUtils.toJavaString(expandToNumberTerm));
            } else {
                expandTo = 0;
            }
        }

        return new OutlineFacet(strategyName, expandTo);
    }
}
