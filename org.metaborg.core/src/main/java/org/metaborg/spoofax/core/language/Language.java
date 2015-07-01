package org.metaborg.spoofax.core.language;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.resource.ResourceService;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

public class Language implements ILanguage {
    private static final long serialVersionUID = 5329634831598144245L;

    private final String name;
    private transient FileObject location;
    private final LanguageVersion version;
    private final int sequenceId;
    private final String id;

    private transient ClassToInstanceMap<ILanguageFacet> facets = MutableClassToInstanceMap.create();
    private transient Subject<LanguageFacetChange, LanguageFacetChange> facetChanges = PublishSubject.create();

    public Language(String name, FileObject location, LanguageVersion version, int sequenceId, String id) {
        this.name = name;
        this.location = location;
        this.version = version;
        this.sequenceId = sequenceId;
        this.id = id;
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

    @Override public int sequenceId() {
        return sequenceId;
    }

    @Override public String id() {
        return id;
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


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + location.hashCode();
        result = prime * result + version.hashCode();
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
        if(!location.equals(other.location))
            return false;
        if(!version.equals(other.version))
            return false;
        return true;
    }

    @Override public String toString() {
        return "Language [name=" + name + ", version=" + version + ", location=" + location + "]";
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        ResourceService.writeFileObject(location, out);

        out.writeInt(facets.size());
        for(ILanguageFacet facet : facets.values())
            out.writeObject(facet);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        location = ResourceService.readFileObject(in);
        facets = MutableClassToInstanceMap.create();
        facetChanges = PublishSubject.create();

        int facetCount = in.readInt();
        for(int i = 0; i < facetCount; i++) {
            final ILanguageFacet facet = (ILanguageFacet) in.readObject();
            facets.put(facet.getClass(), facet);
        }
    }
}
