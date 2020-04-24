package org.metaborg.spoofax.core.syntax;

import javax.annotation.Nullable;

import org.metaborg.core.syntax.ParseFacet;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.terms.util.TermUtils;

public class ParseFacetFromESV {
    public static boolean hasParser(IStrategoAppl esv) {
        return ESVReader.findTerm(esv, "Parser") != null;
    }
    
    public static @Nullable ParseFacet create(IStrategoAppl esv) {
    	final IStrategoAppl parserTerm = ESVReader.findTerm(esv, "Parser");
    	if (parserTerm == null) {
    		return null;
    	}
    	final IStrategoAppl parserType = TermUtils.toApplAt(parserTerm, 0);
    	if(TermUtils.isAppl(parserType, "Named", 1)) {
            return new ParseFacet(ESVReader.termContents(parserType.getSubterm(0)));
        }
        return null;
    }
}