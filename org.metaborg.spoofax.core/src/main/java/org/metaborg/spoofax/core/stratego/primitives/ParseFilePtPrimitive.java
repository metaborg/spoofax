package org.metaborg.spoofax.core.stratego.primitives;

import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ISyntaxService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class ParseFilePtPrimitive extends ParseFilePrimitive {
    @Inject public ParseFilePtPrimitive(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISourceTextService sourceTextService,
        ISyntaxService<IStrategoTerm> syntaxService) {
        super("STRSGLR_parse_string_pt", resourceService, languageIdentifierService, sourceTextService, syntaxService);
    }
}
