package org.metaborg.core.config;

import javax.annotation.Nullable;

import org.metaborg.util.iterators.Iterables2;

public class LangFileExport implements IExport {
    public final String language;
    public final String file;


    public LangFileExport(String languageName, String file) {
        this.language = languageName;
        this.file = file;
    }


    @Override public String languageName() {
        return language;
    }

    @Override public @Nullable String directory() {
        return null;
    }

    @Override public String file() {
        return file;
    }

    @Override public Iterable<String> includes() {
        return Iterables2.empty();
    }

    @Override public Iterable<String> excludes() {
        return Iterables2.empty();
    }
}
