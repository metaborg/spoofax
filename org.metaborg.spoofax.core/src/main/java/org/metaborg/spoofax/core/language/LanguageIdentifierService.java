package org.metaborg.spoofax.core.language;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageIdentifierService implements ILanguageIdentifierService {
    private static final Logger logger = LoggerFactory.getLogger(LanguageIdentifierService.class);

    private final ILanguageService languageService;


    @Inject public LanguageIdentifierService(ILanguageService languageService) {
        this.languageService = languageService;
    }


    @Override public ILanguage identify(FileObject resource) {
        final Collection<String> identifiedLanguageNames = Sets.newHashSet();
        ILanguage identifiedLanguage = null;
        for(ILanguage language : languageService.getAllActive()) {
            if(identify(resource, language)) {
                identifiedLanguageNames.add(language.name());
                identifiedLanguage = language;
            }
        }

        if(identifiedLanguageNames.size() > 1) {
            throw new IllegalStateException("File " + resource + " identifies to multiple languages: "
                + Joiner.on(", ").join(identifiedLanguageNames));
        }

        return identifiedLanguage;
    }

    @Override public boolean identify(FileObject resource, ILanguage language) {
        final IdentificationFacet identification = language.facet(IdentificationFacet.class);
        if(identification == null) {
            logger.error("Cannot identify resources of {}, language does not have an identification facet", language);
            return false;
        }
        return identification.identify(resource);
    }
}
