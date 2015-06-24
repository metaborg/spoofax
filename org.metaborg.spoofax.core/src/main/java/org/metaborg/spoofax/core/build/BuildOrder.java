package org.metaborg.spoofax.core.build;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.LanguagePathFacet;

public class BuildOrder {
 
    public static List<ILanguage> sort(Collection<ILanguage> languages)
            throws SpoofaxException {
        DirectedAcyclicGraph<ILanguage,Object> dag =
                new DirectedAcyclicGraph<>(Object.class);

        // build lookup and vertices
        Map<String,ILanguage> lookup = Maps.newHashMap();
        for ( ILanguage language : languages ) {
            dag.addVertex(language);
            lookup.put(language.name(), language);
        }

        // build graph
        for ( ILanguage source : languages ) {
            LanguagePathFacet facet = source.facet(LanguagePathFacet.class);
            if ( facet != null ) {
                for ( String otherName : facet.sources.keySet() ) {
                    ILanguage target = lookup.get(otherName);
                    if ( target != null ) {
                        try {
                            dag.addDagEdge(source, target);
                        } catch (DirectedAcyclicGraph.CycleFoundException ex) {
                            throw new SpoofaxException("Languages induce generation cycle, cannot determine build order.");
                        }
                    }
                }
            }
        }

        // return sorted languages
        return Lists.newArrayList(dag.iterator());
    }

    private BuildOrder() {
    }

}
