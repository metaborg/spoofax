package org.metaborg.core.config;

import javax.annotation.Nullable;

public class ResourceExport implements IExport {
    public final String directory;
    public final Iterable<String> includes;
    public final Iterable<String> excludes;


    public ResourceExport(String directory, Iterable<String> includes, Iterable<String> excludes) {
        this.directory = directory;
        this.includes = includes;
        this.excludes = excludes;
    }


    @Override public @Nullable String languageName() {
        return null;
    }

    @Override public String directory() {
        return directory;
    }

    @Override public @Nullable String file() {
        return null;
    }

    @Override public Iterable<String> includes() {
        return includes;
    }

    @Override public Iterable<String> excludes() {
        return excludes;
    }
}
