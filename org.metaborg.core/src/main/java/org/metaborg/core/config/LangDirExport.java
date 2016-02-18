package org.metaborg.core.config;

import javax.annotation.Nullable;

import org.metaborg.util.iterators.Iterables2;

public class LangDirExport implements IExport {
    public final String language;
    public final String directory;


    public LangDirExport(String languageName, String directory) {
        this.language = languageName;
        this.directory = directory;
    }


    @Override public String languageName() {
        return language;
    }

    @Override public String directory() {
        return directory;
    }

    @Override public @Nullable String file() {
        return null;
    }

    @Override public Iterable<String> includes() {
        return Iterables2.empty();
    }

    @Override public Iterable<String> excludes() {
        return Iterables2.empty();
    }
}
