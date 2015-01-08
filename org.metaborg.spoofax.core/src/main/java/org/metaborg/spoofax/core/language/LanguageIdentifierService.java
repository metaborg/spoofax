package org.metaborg.spoofax.core.language;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageIdentifierService implements ILanguageIdentifierService {
    private final ILanguageService languageService;


    @Inject public LanguageIdentifierService(ILanguageService languageService) {
        this.languageService = languageService;
    }


    @Override public ILanguage identify(FileObject file) {
        final Collection<String> identifiedLanguageNames = Sets.newHashSet();
        ILanguage identifiedLanguage = null;
        for(ILanguage language : languageService.getAllActive()) {
            final IdentificationFacet identification = language.facet(IdentificationFacet.class);
            if(identification != null && identification.identify(file)) {
                identifiedLanguageNames.add(language.name());
                identifiedLanguage = language;
            }
        }

        if(identifiedLanguageNames.size() > 1) {
            throw new IllegalStateException("File " + file + " identifies to multiple languages: "
                + Joiner.on(", ").join(identifiedLanguageNames));
        }

        return identifiedLanguage;
    }

    @Override public Iterable<ILanguage> identifyAll(FileObject file) {
        final Collection<String> identifiedLanguageNames = Sets.newHashSet();
        final Collection<ILanguage> identifiedLanguages = Lists.newLinkedList();

        for(ILanguage language : languageService.getAll()) {
            final IdentificationFacet identification = language.facet(IdentificationFacet.class);
            if(identification != null && identification.identify(file)) {
                identifiedLanguageNames.add(language.name());
                identifiedLanguages.add(language);
            }
        }

        if(identifiedLanguageNames.size() > 1) {
            throw new IllegalStateException("File " + file + " identifies to multiple languages: "
                + Joiner.on(", ").join(identifiedLanguageNames));
        }

        return identifiedLanguages;
    }
}
