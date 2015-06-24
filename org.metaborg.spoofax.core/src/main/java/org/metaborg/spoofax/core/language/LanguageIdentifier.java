package org.metaborg.spoofax.core.language;

public class LanguageIdentifier {
    private final String id;
    private final LanguageVersion version;


    public LanguageIdentifier(String id, LanguageVersion version) {
        this.id = id;
        this.version = version;
    }


    public String id() {
        return id;
    }

    public LanguageVersion version() {
        return version;
    }

    @Override public String toString() {
        return id + ":" + version;
    }
}
