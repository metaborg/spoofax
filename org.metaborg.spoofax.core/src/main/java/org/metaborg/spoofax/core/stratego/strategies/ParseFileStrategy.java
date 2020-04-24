package org.metaborg.spoofax.core.stratego.strategies;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

import com.google.inject.Inject;

public class ParseFileStrategy extends Strategy {
    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ISpoofaxUnitService unitService;
    private final ISourceTextService sourceTextService;
    private final ISpoofaxSyntaxService syntaxService;


    @Inject public ParseFileStrategy(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISpoofaxUnitService unitService,
        ISourceTextService sourceTextService, ISpoofaxSyntaxService syntaxService) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.unitService = unitService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
    }


    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        if(!TermUtils.isString(current)) return null;

        try {
            final String path = TermUtils.toJavaString(current);
            final FileObject resource = resourceService.resolve(path);
            if(resource.getType() != FileType.FILE) {
                return null;
            }
            final IdentifiedResource identifiedResource = languageIdentifierService.identifyToResource(resource);
            if(identifiedResource == null) {
                return null;
            }
            final String text = sourceTextService.text(resource);
            final ISpoofaxInputUnit input =
                unitService.inputUnit(resource, text, identifiedResource.language, identifiedResource.dialect);
            final ISpoofaxParseUnit result = syntaxService.parse(input);
            return result.ast();
        } catch(ParseException | IOException e) {
            throw new StrategoException("Parsing failed unexpectedly", e);
        }
    }
}
