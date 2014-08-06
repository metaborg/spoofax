package org.metaborg.spoofax.core.service.about;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.language.ILanguageFacet;

public class AboutFacet implements ILanguageFacet {
    public final String description;
    public final String url;


    public AboutFacet(String description, @Nullable String url) {
        this.description = description;
        this.url = url;
    }
}
