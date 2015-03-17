package org.metaborg.spoofax.core.language;

import java.util.Date;

import org.apache.commons.vfs2.FileObject;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.MutableClassToInstanceMap;

public class Language implements ILanguage {
    private final String name;
    private final LanguageVersion version;
    private final FileObject location;
    private final Date createdDate;

    private final ClassToInstanceMap<ILanguageFacet> facets = MutableClassToInstanceMap.create();
    private final Subject<LanguageFacetChange, LanguageFacetChange> facetChanges = PublishSubject.create();


    public Language(String name, LanguageVersion version, FileObject location, Date createdDate) {
        this.name = name;
        this.version = version;
        this.location = location;
        this.createdDate = createdDate;
    }


    @Override public String name() {
        return name;
    }

    @Override public LanguageVersion version() {
        return version;
    }

    @Override public FileObject location() {
        return location;
    }

    @Override public Date createdDate() {
        return createdDate;
    }


    @Override public Iterable<ILanguageFacet> facets() {
        return facets.values();
    }

    @Override public <T extends ILanguageFacet> T facet(Class<T> type) {
        return facets.getInstance(type);
    }

    @Override public Observable<LanguageFacetChange> facetChanges() {
        return facetChanges;
    }

    @Override public <T extends ILanguageFacet> ILanguageFacet addFacet(T facet) {
        @SuppressWarnings("unchecked") final Class<T> type = (Class<T>) facet.getClass();
        if(facets.containsKey(type)) {
            throw new IllegalStateException("Cannot add facet, facet of type " + type + " already exists in language "
                + name);
        }
        facets.putInstance(type, facet);
        facetChanges.onNext(new LanguageFacetChange(facet, LanguageFacetChange.Kind.ADD));
        return facet;
    }

    @Override public <T extends ILanguageFacet> ILanguageFacet removeFacet(Class<T> type) {
        if(!facets.containsKey(type)) {
            throw new IllegalStateException("Cannot remove facet, facet of type " + type
                + " does not exists in language " + name);
        }
        final ILanguageFacet removedFacet = facets.remove(type);
        facetChanges.onNext(new LanguageFacetChange(removedFacet, LanguageFacetChange.Kind.REMOVE));
        return removedFacet;
    }


    @Override public int compareTo(ILanguage other) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(name, other.name())
            .compare(version, other.version())
            .compare(location.getName(), other.location().getName())
            .result();
        // @formatter:on
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + version.hashCode();
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
        final Language other = (Language) obj;
        if(!name.equals(other.name))
            return false;
        if(!version.equals(other.version))
            return false;
        if(!location.equals(other.location))
            return false;
        return true;
    }

    @Override public String toString() {
        return "Language [name=" + name + ", version=" + version + ", location=" + location + "]";
    }
}
