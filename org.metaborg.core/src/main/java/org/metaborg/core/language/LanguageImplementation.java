package org.metaborg.core.language;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.ILanguageImplConfig;
import org.metaborg.core.config.LanguageImplConfig;

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


    @Override public boolean hasFacet(Class<? extends IFacet> type) {
        for(ILanguageComponent component : components) {
            if(component.hasFacet(type)) {
                return true;
            }
        }
        return false;
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
