package org.metaborg.core.project.configuration;

import javax.annotation.Nullable;

public class Export {
    public final @Nullable String languageName;
    public final String directory;
    public final @Nullable Iterable<String> includes;
    public final @Nullable Iterable<String> excludes;

    public Export(@Nullable String languageName, String directory, @Nullable Iterable<String> includes,
        @Nullable Iterable<String> excludes) {
        this.languageName = languageName;
        this.directory = directory;
        this.includes = includes;
        this.excludes = excludes;
    }
}
