package org.metaborg.spoofax.meta.core;

import org.metaborg.spoofax.core.language.LanguageVersion;

public class LanguageDependency {
    
    private final String id;
    private final LanguageVersion version;

    public LanguageDependency(String id, LanguageVersion version) {
        this.id = id;
        this.version = version;
    }

    public String id() {
        return id;
    }

    public LanguageVersion version() {
        return version;
    }

    @Override
    public String toString() {
        return id + ":" + version;
    }

}
