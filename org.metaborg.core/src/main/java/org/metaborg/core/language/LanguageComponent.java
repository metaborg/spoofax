package org.metaborg.core.language;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public class LanguageComponent implements ILanguageComponentInternal {
    private final LanguageIdentifier id;
    private final FileObject location;
    private final int sequenceId;
    private Iterable<ILanguageImplInternal> contributesTo;
    private final ILanguageComponentConfig config;

    private final Multimap<Class<? extends IFacet>, IFacet> facets = ArrayListMultimap.create();


    public LanguageComponent(LanguageIdentifier identifier, FileObject location, int sequenceId,
        Iterable<ILanguageImplInternal> contributesTo, ILanguageComponentConfig config,
        Iterable<? extends IFacet> facets) {
        this.id = identifier;
        this.location = location;
        this.sequenceId = sequenceId;
        this.contributesTo = contributesTo;
        this.config = config;
        for(IFacet facet : facets) {
            this.facets.put(facet.getClass(), facet);
        }
    }


    @Override public LanguageIdentifier id() {
        return id;
    }

    @Override public FileObject location() {
        return location;
    }

    @Override public int sequenceId() {
        return sequenceId;
    }


    @Override public Iterable<? extends ILanguageImpl> contributesTo() {
        return contributesTo;
    }

    @Override public Iterable<? extends ILanguageImplInternal> contributesToInternal() {
        return contributesTo;
    }

    @Override public void clearContributions() {
        contributesTo = Iterables2.empty();
    }


    @Override public ILanguageComponentConfig config() {
        return config;
    }


    @Override public boolean hasFacet(Class<? extends IFacet> type) {
        return facets.containsKey(type);
    }

    @SuppressWarnings("unchecked") @Override public <T extends IFacet> Iterable<T> facets(Class<T> type) {
        return (Iterable<T>) facets.get(type);
    }

    @Override public <T extends IFacet> Iterable<FacetContribution<T>> facetContributions(Class<T> type) {
        final Collection<FacetContribution<T>> contributions = new LinkedList<>();
        for(T facet : facets(type)) {
            contributions.add(new FacetContribution<>(facet, this));
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

    @Override public Iterable<IFacet> facets() {
        return facets.values();
    }

    @Override public Iterable<FacetContribution<IFacet>> facetContributions() {
        final Collection<FacetContribution<IFacet>> contributions = new LinkedList<>();
        for(IFacet facet : facets()) {
            contributions.add(new FacetContribution<>(facet, this));
        }
        return contributions;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + location.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageComponent other = (LanguageComponent) obj;
        if(!id.equals(other.id))
            return false;
        if(!location.equals(other.location))
            return false;
        return true;
    }

    @Override public String toString() {
        return "language comp. " + id + "@" + location;
    }
}
