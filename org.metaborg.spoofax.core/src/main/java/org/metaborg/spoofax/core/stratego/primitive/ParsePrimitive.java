package org.metaborg.spoofax.core.stratego.primitive;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.language.dialect.IDialectIdentifier;
import org.metaborg.core.language.dialect.IdentifiedDialect;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxPrimitive;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class ParsePrimitive extends ASpoofaxPrimitive {
    private final IResourceService resourceService;
    private final ILanguageService languageService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final IDialectIdentifier dialectIdentifier;
    private final ISpoofaxUnitService unitService;
    private final ISourceTextService sourceTextService;
    private final ISpoofaxSyntaxService syntaxService;


    @Inject public ParsePrimitive(IResourceService resourceService, ILanguageService languageService,
        ILanguageIdentifierService languageIdentifierService, IDialectIdentifier dialectIdentifier,
        ISpoofaxUnitService unitService, ISourceTextService sourceTextService, ISpoofaxSyntaxService syntaxService) {
        super("parse", 0, 4);
        this.resourceService = resourceService;
        this.languageService = languageService;
        this.languageIdentifierService = languageIdentifierService;
        this.dialectIdentifier = dialectIdentifier;
        this.unitService = unitService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, org.spoofax.interpreter.core.IContext strategoContext)
        throws MetaborgException, IOException {
        // Determine what to parse.
        if(!(current instanceof IStrategoString)) {
            throw new MetaborgException("Cannot parse, input string or file " + current + " is not a string");
        }
        final String stringOrFile = ((IStrategoString) current).stringValue();

        final String text;
        final @Nullable FileObject file;
        final IStrategoTerm isFileTerm = tvars[0];
        if(!(isFileTerm instanceof IStrategoInt)) {
            throw new MetaborgException("Cannot parse, input kind " + isFileTerm + " is not an integer");
        }
        if(((IStrategoInt) isFileTerm).intValue() == 1) {
            file = resourceService.resolve(stringOrFile);
            if(!file.exists() || !file.isFile()) {
                throw new MetaborgException("Cannot parse, input file " + file + " does not exist or is not a file");
            }
            text = sourceTextService.text(file);
        } else {
            file = null;
            text = stringOrFile;
        }

        // Determine which language to parse it with.
        final ILanguageImpl langImpl;
        final IStrategoTerm nameOrGroupIdTerm = tvars[1];
        final IStrategoTerm idTerm = tvars[2];
        final IStrategoTerm versionTerm = tvars[3];

        if(nameOrGroupIdTerm instanceof IStrategoTuple) {
            // No name, groupId, id, and version was set, auto detect language to parse with.
            if(file == null) {
                throw new MetaborgException("Cannot parse a string, no language to parse it with was given");
            }

            final IContext context = metaborgContext(strategoContext);
            if(context != null) {
                langImpl = languageIdentifierService.identify(file, context.project());
            } else {
                langImpl = languageIdentifierService.identify(file);
            }
            if(langImpl == null) {
                throw new MetaborgException("Cannot parse, language for " + file + " could not be identified");
            }
        } else if(idTerm instanceof IStrategoTuple) {
            // No id was set, name is set.
            if(!(nameOrGroupIdTerm instanceof IStrategoString)) {
                throw new MetaborgException("Cannot parse, language name " + nameOrGroupIdTerm + " is not a string");
            }
            final String name = ((IStrategoString) nameOrGroupIdTerm).stringValue();

            final ILanguage lang = languageService.getLanguage(name);
            if(lang == null) {
                throw new MetaborgException("Cannot parse, language " + nameOrGroupIdTerm + " does not exist");
            }
            langImpl = lang.activeImpl();
            if(langImpl == null) {
                throw new MetaborgException("Cannot parse, language " + lang + " has no implementation loaded");
            }
        } else {
            // A groupId, id, and version is set.
            if(!(nameOrGroupIdTerm instanceof IStrategoString)) {
                throw new MetaborgException("Cannot parse, language groupId " + nameOrGroupIdTerm + " is not a string");
            }
            final String groupId = ((IStrategoString) nameOrGroupIdTerm).stringValue();
            if(!(idTerm instanceof IStrategoString)) {
                throw new MetaborgException("Cannot parse, language id " + idTerm + " is not a string");
            }
            final String id = ((IStrategoString) idTerm).stringValue();
            if(!(versionTerm instanceof IStrategoString)) {
                throw new MetaborgException("Cannot parse, language version " + versionTerm + " is not a string");
            }
            final String versionStr = ((IStrategoString) versionTerm).stringValue();
            final LanguageVersion version = LanguageVersion.parse(versionStr);

            final LanguageIdentifier langId = new LanguageIdentifier(groupId, id, version);
            langImpl = languageService.getImpl(langId);
            if(langImpl == null) {
                throw new MetaborgException("Cannot parse, language implementation " + langId + " does not exist");
            }
        }

        // Parse the text.
        final ISpoofaxInputUnit input;
        if(file != null) {
            @Nullable ILanguageImpl dialect;
            try {
                final IdentifiedDialect identifierDialect = dialectIdentifier.identify(file);
                if(identifierDialect != null) {
                    dialect = identifierDialect.dialect;
                } else {
                    dialect = null;
                }
            } catch(MetaborgException | MetaborgRuntimeException e) {
                // Ignore
                dialect = null;
            }
            input = unitService.inputUnit(file, text, langImpl, dialect);
        } else {
            input = unitService.inputUnit(text, langImpl, null);
        }
        final ISpoofaxParseUnit result = syntaxService.parse(input);
        if(result.valid() && result.success()) {
            return result.ast();
        } else {
            return null;
        }
    }
}
