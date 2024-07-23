package org.metaborg.core.language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.ILanguageImplConfig;
import org.metaborg.core.config.LanguageImplConfig;
import org.metaborg.util.iterators.Iterables2;

public class LanguageImplementation implements ILanguageImpl, ILanguageImplInternal {
    private final LanguageIdentifier id;
    private final ILanguageInternal belongsTo;

    private final Set<ILanguageComponent> components = new HashSet<ILanguageComponent>();


    public LanguageImplementation(LanguageIdentifier id, ILanguageInternal belongsTo) {
        this.id = id;
        this.belongsTo = belongsTo;
    }


    @Override public LanguageIdentifier id() {
        return id;
    }

    @Override public int sequenceId() {
        int max = Integer.MIN_VALUE;
        for(ILanguageComponent component : components) {
            max = Math.max(component.sequenceId(), max);
        }
        return max;
    }

    @Override public List<FileObject> locations() {
        final List<FileObject> locations = new LinkedList<>();
        for(ILanguageComponent component : components) {
            locations.add(component.location());
        }
        return locations;
    }

    @Override public Set<ILanguageComponent> components() {
        return components;
    }

    @Override public ILanguage belongsTo() {
        return belongsTo;
    }

    @Override public ILanguageInternal belongsToInternal() {
        return belongsTo;
    }


    @Override public ILanguageImplConfig config() {
        final Collection<ILanguageComponentConfig> configs = new ArrayList<>(components.size());
        for(ILanguageComponent component : components) {
            configs.add(component.config());
        }
        return new LanguageImplConfig(configs);
    }


    @Override public boolean hasFacet(Class<? extends IFacet> type) {
        for(ILanguageComponent component : components) {
            if(component.hasFacet(type)) {
                return true;
            }
        }
        return false;
    }

    @Override public <T extends IFacet> Iterable<T> facets(Class<T> type) {
        final Collection<T> facets = new LinkedList<>();
        for(ILanguageComponent component : components) {
            Iterables2.addAll(facets, component.facets(type));
        }
        return facets;
    }

    @Override public <T extends IFacet> Iterable<FacetContribution<T>> facetContributions(Class<T> type) {
        final Collection<FacetContribution<T>> contributions = new LinkedList<>();
        for(ILanguageComponent component : components) {
            Iterables2.addAll(contributions, component.facetContributions(type));
        }
        return contributions;
    }

    @Override public <T extends IFacet> T facet(Class<T> type) {
        // GTODO: code duplication with LanguageComponent, use default interface implementation in Java 8.
        final Iterable<T> facets = facets(type);
        final int size = Iterables2.size(facets);
        if(size == 0) {
            return null;
        } else if(size > 1) {
            throw new MetaborgRuntimeException(
                "Multiple facets of " + type + " found in language implementation " + id + ", while only a single facet is supported");
        }
        return facets.iterator().next();
    }

    @Override public <T extends IFacet> FacetContribution<T> facetContribution(Class<T> type) {
        // GTODO: code duplication with LanguageComponent, use default interface implementation in Java 8.
        final Iterable<FacetContribution<T>> facetContributions = facetContributions(type);
        final int size = Iterables2.size(facetContributions);
        if(size == 0) {
            return null;
        } else if(size > 1) {
            throw new MetaborgRuntimeException(
                "Multiple facets of " + type + " found in language implementation " + id + ", while only a single facet is supported");
        }
        return facetContributions.iterator().next();
    }

    @Override public Iterable<IFacet> facets() {
        final Collection<IFacet> facets = new LinkedList<>();
        for(ILanguageComponent component : components) {
            Iterables2.addAll(facets, component.facets());
        }
        return facets;
    }

    @Override public Iterable<FacetContribution<IFacet>> facetContributions() {
        final Collection<FacetContribution<IFacet>> contributions = new LinkedList<>();
        for(ILanguageComponent component : components) {
            Iterables2.addAll(contributions, component.facetContributions());
        }
        return contributions;
    }


    @Override public boolean addComponent(ILanguageComponent component) {
        return components.add(component);
    }

    @Override public boolean removeComponent(ILanguageComponent component) {
        return components.remove(component);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageImplementation other = (LanguageImplementation) obj;
        if(!id.equals(other.id))
            return false;
        return true;
    }

    @Override public String toString() {
        return "language impl. " + id;
    }
}
