package org.metaborg.spoofax.core.stratego.primitive.legacy.parse;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class LegacyParseFilePrimitive extends AbstractPrimitive {
    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ISpoofaxUnitService unitService;
    private final ISourceTextService sourceTextService;
    private final ISpoofaxSyntaxService syntaxService;


    @Inject public LegacyParseFilePrimitive(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISpoofaxUnitService unitService,
        ISourceTextService sourceTextService, ISpoofaxSyntaxService syntaxService) {
        this("STRSGLR_parse_string", resourceService, languageIdentifierService, unitService, sourceTextService,
            syntaxService);
    }

    protected LegacyParseFilePrimitive(String name, IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISpoofaxUnitService unitService,
        ISourceTextService sourceTextService, ISpoofaxSyntaxService syntaxService) {
        super(name, 1, 4);

        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.unitService = unitService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
    }


    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        if(!Tools.isTermString(terms[0]))
            return false;
        if(!Tools.isTermString(terms[3]))
            return false;

        try {
            final String pathOrInput = Tools.asJavaString(terms[0]);
            final String pathOrInput2 = Tools.asJavaString(terms[3]);
            FileObject resource;
            String text;
            try {
                resource = resourceService.resolve(pathOrInput);
                if(!resource.exists() || resource.getType() != FileType.FILE) {
                    resource = resourceService.resolve(pathOrInput2);
                    text = pathOrInput;
                } else {
                    text = sourceTextService.text(resource);
                }
            } catch(MetaborgRuntimeException | IOException e) {
                resource = resourceService.resolve(pathOrInput2);
                text = pathOrInput;
            }

            if(resource.getType() != FileType.FILE) {
                return false;
            }

            final IdentifiedResource identifiedResource = languageIdentifierService.identifyToResource(resource);
            if(identifiedResource == null) {
                return false;
            }
            final ISpoofaxInputUnit input =
                unitService.inputUnit(resource, text, identifiedResource.language, identifiedResource.dialect);
            final ISpoofaxParseUnit result = syntaxService.parse(input);
            if(result.valid() && result.success()) {
                env.setCurrent(result.ast());
            } else {
                return false;
            }
        } catch(ParseException | IOException e) {
            throw new InterpreterException("Parsing failed unexpectedly", e);
        }

        return true;
    }
}
