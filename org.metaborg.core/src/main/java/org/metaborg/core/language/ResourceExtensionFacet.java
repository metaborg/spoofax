package org.metaborg.core.language;

/**
 * Represents a facet that specifies the file extensions for languages. Complements the {@link IdentificationFacet}.
 */
public class ResourceExtensionFacet implements IFacet {
    private final Iterable<String> extensions;


    public ResourceExtensionFacet(Iterable<String> extensions) {
        this.extensions = extensions;
    }


    public Iterable<String> extensions() {
        return extensions;
    }
}
