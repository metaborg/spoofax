package org.metaborg.spoofax.core.language;

import java.util.Date;
import java.util.Set;

import org.apache.commons.vfs2.FileName;
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
    private final FileName location;
    private final Set<String> extensions;
    private final Iterable<FileObject> resources;
    private final Date loadedDate;

    private final ClassToInstanceMap<ILanguageFacet> facets = MutableClassToInstanceMap.create();
    private final Subject<LanguageFacetChange, LanguageFacetChange> facetChanges = PublishSubject.create();


    public Language(String name, LanguageVersion version, FileName location, Set<String> extensions,
        Iterable<FileObject> resources, Date loadedDate) {
        this.name = name;
        this.version = version;
        this.location = location;
        this.extensions = extensions;
        this.resources = resources;
        this.loadedDate = loadedDate;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public LanguageVersion version() {
        return version;
    }

    @Override
    public FileName location() {
        return location;
    }

    @Override
    public Iterable<String> extensions() {
        return extensions;
    }

    @Override
    public boolean hasExtension(String extension) {
        return extensions.contains(extension);
    }

    @Override
    public Iterable<FileObject> resources() {
        return resources;
    }

    @Override
    public Date loadedDate() {
        return loadedDate;
    }


    @Override
    public Iterable<ILanguageFacet> facets() {
        return facets.values();
    }

    @Override
    public <T extends ILanguageFacet> T facet(Class<T> type) {
        return facets.getInstance(type);
    }

    @Override
    public Observable<LanguageFacetChange> facetChanges() {
        return facetChanges;
    }

    @Override
    public <T extends ILanguageFacet> void addFacet(Class<T> type, T facet) {
        facets.putInstance(type, facet);
        facetChanges.onNext(new LanguageFacetChange(facet, LanguageFacetChange.Kind.ADDED));
    }

    @Override
    public <T extends ILanguageFacet> ILanguageFacet removeFacet(Class<T> type) {
        final ILanguageFacet removedFacet = facets.remove(type);
        facetChanges.onNext(new LanguageFacetChange(removedFacet, LanguageFacetChange.Kind.REMOVED));
        return removedFacet;
    }


    @Override
    public int compareTo(ILanguage other) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(name, other.name())
            .compare(version, other.version())
            .compare(loadedDate, other.loadedDate())
            .compare(location, other.location())
            .result();
        // @formatter:on
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + version.hashCode();
        result = prime * result + location.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
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

    @Override
    public String toString() {
        return "Language [name=" + name + ", version=" + version + ", location=" + location + ", extensions="
            + extensions + "]";
    }
}
