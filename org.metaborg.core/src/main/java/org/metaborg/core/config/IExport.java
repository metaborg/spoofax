package org.metaborg.core.config;

import javax.annotation.Nullable;

public interface IExport {
    @Nullable String languageName();

    @Nullable String directory();

    @Nullable String file();

    Iterable<String> includes();

    Iterable<String> excludes();
}
