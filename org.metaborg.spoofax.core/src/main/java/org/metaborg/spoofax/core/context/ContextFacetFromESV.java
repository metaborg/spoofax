package org.metaborg.spoofax.core.context;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class ContextFacetFromESV {
    public static boolean hasContext(IStrategoAppl esv) {
        return ESVReader.findTerm(esv, "Context") != null;
    }
    
    public static @Nullable String type(IStrategoAppl esv) {
        final IStrategoAppl contextTerm = ESVReader.findTerm(esv, "Context");
        if(contextTerm == null) {
            return null;
        }
        final IStrategoAppl typeTerm = (IStrategoAppl) contextTerm.getSubterm(0); 

        switch(typeTerm.getConstructor().getName()) {
            case "None":
                return null;
            case "Legacy":
                return LegacyContextFactory.name;
            case "TaskEngine":
                return AnalysisContextFactory.name;
            default:
                return null;
        }
    }
}
