package org.metaborg.spoofax.core.language;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.ArrayList;
import java.util.List;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;

/**
 * Create ComponentsFacet from ESV definition
 */
public class LanguagePathFacetFromESV {
    private static final Logger log = LoggerFactory.getLogger(LanguagePathFacetFromESV.class);
    
    public static LanguagePathFacet create(IStrategoAppl esv) {
        ListMultimap<String, String> sources = readPathTerms(esv, "LanguageSources");
        ListMultimap<String, String> includes = readPathTerms(esv, "LanguageIncludes");
        return new LanguagePathFacet(sources, includes);
    }

    private static ListMultimap<String, String> readPathTerms(IStrategoAppl esv, String constructor) {
        ArrayList<IStrategoAppl> terms = ESVReader.collectTerms(esv, constructor);
        ListMultimap<String,String> languagePath = ArrayListMultimap.create();
        for ( IStrategoAppl term : terms ) {
            String language = ESVReader.termContents(Tools.termAt(term,0));
            List<String> components = ESVReader.termListContents(Tools.termAt(term,1));
            languagePath.putAll(language, components);
        }
        return languagePath;
    }

}
