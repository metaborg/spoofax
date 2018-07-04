package org.metaborg.spoofax.core.context;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class ContextFacetFromESV {

    private static final ILogger logger = LoggerUtils.logger(ContextFacetFromESV.class);

    public static boolean hasContext(IStrategoAppl esv) {
        return ESVReader.findTerm(esv, "Context") != null;
    }
    
    public static @Nullable String type(IStrategoAppl esv) {
        final IStrategoAppl contextTerm = ESVReader.findTerm(esv, "Context");
        if(contextTerm == null) {
            return null;
        }
        final IStrategoAppl typeTerm = (IStrategoAppl) contextTerm.getSubterm(0); 

        final String name = typeTerm.getConstructor().getName();
        switch(name) {
            case "None":
                return null;
            default:
                logger.warn("Unknown context type {}, defaulting to legacy context.", name);
            case "Legacy":
                return LegacyContextFactory.name;
            case "TaskEngine":
                return IndexTaskContextFactory.name;
        }
    }
}
