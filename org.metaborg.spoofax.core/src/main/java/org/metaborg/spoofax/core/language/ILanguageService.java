package org.metaborg.spoofax.core.language;

import org.apache.commons.vfs2.FileName;

import rx.Observable;

import com.google.common.collect.ImmutableSet;

public interface ILanguageService {
    public ILanguage get(String name);

    public ILanguage get(String name, LanguageVersion version, FileName location);

    public ILanguage getByExt(String extension);

    public Iterable<ILanguage> getAll(String name);

    public Iterable<ILanguage> getAll(String name, LanguageVersion version);

    public Iterable<ILanguage> getAllByExt(String extension);

    public Observable<LanguageChange> changes();

    public ILanguage create(String name, LanguageVersion version, FileName location, ImmutableSet<String> extensions);

    public void remove(ILanguage language);
}
