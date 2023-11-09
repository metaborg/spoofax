package org.metaborg.core.language;

import jakarta.annotation.Nullable;

/**
 * Represents a facet that provides basic information about a language
 */
public class DescriptionFacet implements IFacet {
    /**
     * Description of the language.
     */
    public final String description;

    /**
     * Website URL of the language, or null if none.
     */
    @Nullable public final String url;


    /**
     * Creates an 'about' facet from a description and URL.
     * 
     * @param description
     *            Description of the language.
     * @param url
     *            Website URL of the language, or null if none.
     */
    public DescriptionFacet(String description, @Nullable String url) {
        this.description = description;
        this.url = url;
    }
}
