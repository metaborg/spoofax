package org.metaborg.spoofax.core.outline;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

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
            if(expandToNumberTerm instanceof IStrategoInt) {
                final IStrategoInt expandToNumberIntTerm = (IStrategoInt) expandToNumberTerm;
                expandTo = expandToNumberIntTerm.intValue();
            } else if(expandToNumberTerm instanceof IStrategoString) {
                final IStrategoString expandToNumberStringTerm = (IStrategoString) expandToNumberTerm;
                expandTo = Integer.parseInt(expandToNumberStringTerm.stringValue());
            } else {
                expandTo = 0;
            }
        }

        return new OutlineFacet(strategyName, expandTo);
    }
}
