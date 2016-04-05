package org.metaborg.spoofax.core.stratego.primitives;

import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.syntax.SpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;

import com.google.inject.Inject;

public class ParseFilePtPrimitive extends ParseFilePrimitive {
    @Inject public ParseFilePtPrimitive(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISpoofaxUnitService unitService,
        ISourceTextService sourceTextService, SpoofaxSyntaxService syntaxService) {
        super("STRSGLR_parse_string_pt", resourceService, languageIdentifierService, unitService, sourceTextService,
            syntaxService);
    }
}
