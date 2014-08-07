package org.metaborg.spoofax.core;

import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.resource.IResourceService;

import com.google.inject.Inject;

public class SpoofaxSession {
    public final IResourceService resource;
    public final ILanguageService language;

    @Inject public SpoofaxSession(IResourceService resource, ILanguageService language) {
        this.resource = resource;
        this.language = language;
    }
}
