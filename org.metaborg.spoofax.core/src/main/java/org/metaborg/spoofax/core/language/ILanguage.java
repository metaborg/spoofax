package org.metaborg.spoofax.core.language;

import java.util.Date;

import org.apache.commons.vfs2.FileName;

import rx.Observable;

public interface ILanguage extends Comparable<ILanguage> {
    public String name();

    public LanguageVersion version();

    public FileName location();

    public Iterable<String> extensions();

    public boolean hasExtension(String extension);

    public Date loadedDate();


    public Iterable<ILanguageFacet> facets();

    public <T extends ILanguageFacet> T facet(Class<T> type);

    public Observable<LanguageFacetChange> facetChanges();

    public <T extends ILanguageFacet> ILanguageFacet addFacet(Class<T> type, T facet);

    public <T extends ILanguageFacet> ILanguageFacet removeFacet(Class<T> type);
}
