package org.metaborg.spoofax.core.language;

import java.util.Collection;

import org.metaborg.core.language.LanguagePathFacet;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Create ComponentsFacet from ESV definition
 */
public class LanguagePathFacetFromESV {
    public static LanguagePathFacet create(IStrategoAppl esv) {
        final ListMultimap<String, String> sources = readPathTerms(esv, "LanguageSources");
        final ListMultimap<String, String> includes = readPathTerms(esv, "LanguageIncludes");
        return new LanguagePathFacet(sources, includes);
    }

    private static ListMultimap<String, String> readPathTerms(IStrategoAppl esv, String constructor) {
        final Iterable<IStrategoAppl> terms = ESVReader.collectTerms(esv, constructor);
        final ListMultimap<String, String> languagePath = ArrayListMultimap.create();
        for(IStrategoAppl term : terms) {
            final String language = ESVReader.termContents(Tools.termAt(term, 0));
            final Collection<String> components = ESVReader.termListContents(Tools.termAt(term, 1));
            languagePath.putAll(language, components);
        }
        return languagePath;
    }
}
