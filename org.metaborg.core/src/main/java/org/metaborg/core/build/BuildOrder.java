package org.metaborg.core.build;

import java.util.Iterator;
import java.util.Map;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.LanguagePathFacet;
import org.metaborg.util.iterators.Iterables2;

import rx.functions.Func0;

import com.google.common.collect.Maps;

/**
 * Language build order calculation.
 */
public class BuildOrder {
    private final Iterable<ILanguage> languages;
    private final DirectedAcyclicGraph<ILanguage, Object> dag;


    /**
     * Creates a build order for given languages, using their dependencies.
     * 
     * @param languages
     *            Languages to create a build order for.
     * @throws MetaborgRuntimeException
     *             When there is a cyclic dependency between languages.
     */
    public BuildOrder(Iterable<ILanguage> languages) throws MetaborgRuntimeException {
        this.languages = languages;
        this.dag = new DirectedAcyclicGraph<ILanguage, Object>(Object.class);

        final Map<String, ILanguage> lookup = Maps.newHashMap();
        for(ILanguage language : languages) {
            dag.addVertex(language);
            lookup.put(language.name(), language);
        }

        for(ILanguage source : languages) {
            final LanguagePathFacet facet = source.facet(LanguagePathFacet.class);
            if(facet != null) {
                for(String otherName : facet.sources.keySet()) {
                    final ILanguage target = lookup.get(otherName);
                    if(target != null) {
                        try {
                            dag.addDagEdge(source, target);
                        } catch(DirectedAcyclicGraph.CycleFoundException e) {
                            throw new MetaborgRuntimeException(
                                "Languages induce build cycle, cannot determine build order", e);
                        }
                    }
                }
            }
        }
    }


    /**
     * @return Build order.
     */
    public Iterable<ILanguage> buildOrder() {
        return Iterables2.from(new Func0<Iterator<ILanguage>>() {
            @Override public Iterator<ILanguage> call() {
                return dag.iterator();
            }
        });
    }

    /**
     * @return Languages in this build order, in the same order that they were passed in the constructor.
     */
    public Iterable<ILanguage> languages() {
        return languages;
    }
}
