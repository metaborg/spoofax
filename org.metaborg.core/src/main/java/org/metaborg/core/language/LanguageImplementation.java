package org.metaborg.core.language;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.config.ILanguageComponentConfig;
import org.metaborg.core.project.config.ILanguageImplConfig;
import org.metaborg.core.project.config.LanguageImplConfig;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LanguageImplementation implements ILanguageImpl, ILanguageImplInternal {
    private final LanguageIdentifier id;
    private final ILanguageInternal belongsTo;

    private final Set<ILanguageComponent> components = Sets.newHashSet();


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

    @Override public Iterable<FileObject> locations() {
        final Collection<FileObject> locations = Lists.newLinkedList();
        for(ILanguageComponent component : components) {
            locations.add(component.location());
        }
        return locations;
    }

    @Override public Iterable<ILanguageComponent> components() {
        return components;
    }

    @Override public ILanguage belongsTo() {
        return belongsTo;
    }

    @Override public ILanguageInternal belongsToInternal() {
        return belongsTo;
    }


    @Override public ILanguageImplConfig config() {
        final Collection<ILanguageComponentConfig> configs = Lists.newArrayListWithCapacity(components.size());
        for(ILanguageComponent component : components) {
            configs.add(component.config());
        }
        return new LanguageImplConfig(configs);
    }

    @Override public Iterable<IFacet> facets() {
        final Collection<IFacet> facets = Lists.newLinkedList();
        for(ILanguageComponent component : components) {
            Iterables.addAll(facets, component.facets());
        }
        return facets;
    }

    @Override public Iterable<FacetContribution<IFacet>> facetContributions() {
        final Collection<FacetContribution<IFacet>> contributions = Lists.newLinkedList();
        for(ILanguageComponent component : components) {
            Iterables.addAll(contributions, component.facetContributions());
        }
        return contributions;
    }

    @Override public <T extends IFacet> Iterable<T> facets(Class<T> type) {
        final Collection<T> facets = Lists.newLinkedList();
        for(ILanguageComponent component : components) {
            Iterables.addAll(facets, component.facets(type));
        }
        return facets;
    }

    @Override public <T extends IFacet> Iterable<FacetContribution<T>> facetContributions(Class<T> type) {
        final Collection<FacetContribution<T>> contributions = Lists.newLinkedList();
        for(ILanguageComponent component : components) {
            Iterables.addAll(contributions, component.facetContributions(type));
        }
        return contributions;
    }

    @Override public <T extends IFacet> T facet(Class<T> type) {
        // GTODO: code duplication with LanguageComponent, use default interface implementation in Java 8.
        final Iterable<T> facets = facets(type);
        final int size = Iterables.size(facets);
        if(size == 0) {
            return null;
        } else if(size > 1) {
            throw new MetaborgRuntimeException(
                "Multiple facets of type " + type + " found, while only a single facet is supported");
        }
        return Iterables.get(facets, 0);
    }

    @Override public <T extends IFacet> FacetContribution<T> facetContribution(Class<T> type) {
        // GTODO: code duplication with LanguageComponent, use default interface implementation in Java 8.
        final Iterable<FacetContribution<T>> facetContributions = facetContributions(type);
        final int size = Iterables.size(facetContributions);
        if(size == 0) {
            return null;
        } else if(size > 1) {
            throw new MetaborgRuntimeException(
                "Multiple facets of type " + type + " found, while only a single facet is supported");
        }
        return Iterables.get(facetContributions, 0);
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
