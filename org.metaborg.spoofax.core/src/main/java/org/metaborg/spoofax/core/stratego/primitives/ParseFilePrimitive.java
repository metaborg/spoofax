package org.metaborg.spoofax.core.stratego.primitives;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.source.ISourceTextService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class ParseFilePrimitive extends AbstractPrimitive {
    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ISourceTextService sourceTextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;


    @Inject public ParseFilePrimitive(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISourceTextService sourceTextService,
        ISyntaxService<IStrategoTerm> syntaxService) {
        this("STRSGLR_parse_string", resourceService, languageIdentifierService,
                sourceTextService, syntaxService);
    }

    protected ParseFilePrimitive(String name, IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ISourceTextService sourceTextService,
        ISyntaxService<IStrategoTerm> syntaxService) {
        super(name, 1, 4);

        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
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
            String input;
            try {
                resource = resourceService.resolve(pathOrInput);
                if(!resource.exists() || resource.getType() != FileType.FILE) {
                    resource = resourceService.resolve(pathOrInput2);
                    input = pathOrInput;
                } else {
                    input = sourceTextService.text(resource);
                }
            } catch(SpoofaxRuntimeException | IOException e) {
                resource = resourceService.resolve(pathOrInput2);
                input = pathOrInput;
            }

            if(resource.getType() != FileType.FILE) {
                return false;
            }

            final ILanguage language = languageIdentifierService.identify(resource);
            if(language == null) {
                return false;
            }
            final ParseResult<IStrategoTerm> result = syntaxService.parse(input, resource, language, null);
            if(result.result == null) {
                return false;
            }
            env.setCurrent(result.result);
        } catch(ParseException | IOException e) {
            throw new InterpreterException("Parsing failed", e);
        }

        return true;
    }
}
