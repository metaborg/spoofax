package org.metaborg.spoofax.core.stratego.primitive.legacy.parse;

import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.syntax.SpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;


public class LegacyParseFilePtPrimitive extends LegacyParseFilePrimitive {
    @jakarta.inject.Inject public LegacyParseFilePtPrimitive(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISpoofaxUnitService unitService,
        ISourceTextService sourceTextService, SpoofaxSyntaxService syntaxService) {
        super("STRSGLR_parse_string_pt", resourceService, languageIdentifierService, unitService, sourceTextService,
            syntaxService);
    }
}
