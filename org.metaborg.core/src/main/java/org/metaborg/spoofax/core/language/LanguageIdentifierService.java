package org.metaborg.spoofax.core.language;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.language.dialect.IDialectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageIdentifierService implements ILanguageIdentifierService {
    private static final Logger logger = LoggerFactory.getLogger(LanguageIdentifierService.class);

    private final ILanguageService languageService;
    private final IDialectIdentifier dialectIdentifier;


    @Inject public LanguageIdentifierService(ILanguageService languageService, IDialectIdentifier dialectIdentifier) {
        this.languageService = languageService;
        this.dialectIdentifier = dialectIdentifier;
    }


    @Override public ILanguage identify(FileObject resource) {
        return identify(resource, languageService.getAllActive());
    }

    @Override public boolean identify(FileObject resource, ILanguage language) {
        final IdentificationFacet identification = language.facet(IdentificationFacet.class);
        if(identification == null) {
            logger.error("Cannot identify resources of {}, language does not have an identification facet", language);
            return false;
        }
        return identification.identify(resource);
    }


    @Override public ILanguage identify(FileObject resource, Iterable<ILanguage> languages) {
        // Ignore directories.
        try {
            if(resource.getType() == FileType.FOLDER) {
                return null;
            }
        } catch(FileSystemException e1) {
            return null;
        }
        
        // Try to identify using the dialect identifier first.
        try {
            final ILanguage dialect = dialectIdentifier.identify(resource);
            if(dialect != null) {
                return dialect;
            }
        } catch(SpoofaxException e) {
            logger.error("Error identifying dialect", e);
            return null;
        } catch(SpoofaxRuntimeException e) {
            // Ignore
        }

        // Identify using identification facet.
        final Set<String> identifiedLanguageNames = Sets.newLinkedHashSet();
        ILanguage identifiedLanguage = null;
        for(ILanguage language : languages) {
            if(identify(resource, language)) {
                identifiedLanguageNames.add(language.name());
                identifiedLanguage = language;
            }
        }

        if(identifiedLanguageNames.size() > 1) {
            throw new IllegalStateException("Resource " + resource + " identifies to multiple languages: "
                + Joiner.on(", ").join(identifiedLanguageNames));
        }

        return identifiedLanguage;
    }
}
