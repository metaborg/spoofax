package org.metaborg.core.build;

import java.util.Iterator;
import java.util.Map;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguagePathFacet;
import org.metaborg.util.iterators.Iterables2;

import rx.functions.Func0;

import com.google.common.collect.Maps;

/**
 * Language build order calculation.
 */
public class BuildOrder {
    private final Iterable<ILanguageImpl> languages;
    private final DirectedAcyclicGraph<ILanguageImpl, Object> dag;


    /**
     * Creates a build order for given languages, using their dependencies.
     * 
     * @param languages
     *            Languages to create a build order for.
     * @throws MetaborgRuntimeException
     *             When there is a cyclic dependency between languages.
     */
    public BuildOrder(Iterable<ILanguageImpl> languages) throws MetaborgRuntimeException {
        this.languages = languages;
        this.dag = new DirectedAcyclicGraph<ILanguageImpl, Object>(Object.class);

        final Map<String, ILanguageImpl> lookup = Maps.newHashMap();
        for(ILanguageImpl language : languages) {
            dag.addVertex(language);
            lookup.put(language.belongsTo().name(), language);
        }

        for(ILanguageImpl source : languages) {
            final Iterable<LanguagePathFacet> facets = source.facets(LanguagePathFacet.class);
            for(LanguagePathFacet facet : facets) {
                for(String otherName : facet.sources.keySet()) {
                    final ILanguageImpl target = lookup.get(otherName);
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
    public Iterable<ILanguageImpl> buildOrder() {
        return Iterables2.from(new Func0<Iterator<ILanguageImpl>>() {
            @Override public Iterator<ILanguageImpl> call() {
                return dag.iterator();
            }
        });
    }

    /**
     * @return Languages in this build order, in the same order that they were passed in the constructor.
     */
    public Iterable<ILanguageImpl> languages() {
        return languages;
    }
}
