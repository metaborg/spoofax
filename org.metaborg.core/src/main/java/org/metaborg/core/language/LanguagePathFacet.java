package org.metaborg.core.language;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Facet to describe language components
 */
public class LanguagePathFacet implements IFacet {
    public final Multimap<String, String> sources;
    public final Multimap<String, String> includes;


    public LanguagePathFacet(ListMultimap<String, String> sources, ListMultimap<String, String> includes) {
        this.sources = Multimaps.unmodifiableListMultimap(sources);
        this.includes = Multimaps.unmodifiableListMultimap(includes);
    }
}
