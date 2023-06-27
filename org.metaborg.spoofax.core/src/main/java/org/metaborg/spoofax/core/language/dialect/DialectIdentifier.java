package org.metaborg.spoofax.core.language.dialect;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.dialect.IDialectIdentifier;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.language.dialect.IdentifiedDialect;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;

import javax.inject.Inject;
import org.spoofax.terms.util.TermUtils;

public class DialectIdentifier implements IDialectIdentifier {
    private static final ILogger logger = LoggerUtils.logger(DialectIdentifier.class);

    private final ILanguageService languageService;
    private final IDialectService dialectService;
    private final ITermFactory termFactory;


    @Inject public DialectIdentifier(ILanguageService languageService, IDialectService dialectService,
        ITermFactory termFactory) {
        this.languageService = languageService;
        this.dialectService = dialectService;
        this.termFactory = termFactory;
    }


    // FIXME: why is this restricted to Stratego files?
    @Override public IdentifiedDialect identify(FileObject resource) throws MetaborgException {
        final ILanguage strategoLanguage = languageService.getLanguage(SpoofaxConstants.LANG_STRATEGO_NAME);
        if(strategoLanguage == null) {
            final String message = logger.format(
                "Could not find Stratego language, Stratego dialects cannot be identified for resource: {}", resource);
            throw new MetaborgRuntimeException(message);
        }

        // GTODO: use identifier service instead, but that introduces a cyclic dependency. Could use a provider.
        final ILanguageImpl strategoImpl = strategoLanguage.activeImpl();
        if(strategoImpl == null) {
            return null;
        }
        // HACK: assuming single identification facet
        final IdentificationFacet facet = strategoImpl.facet(IdentificationFacet.class);
        // HACK: allow str2 files from the Stratego 2 language to be identified too.
        if(facet == null || !(facet.identify(resource) || resource.getName().getExtension().equals("str2"))) {
            return null;
        }

        try {
            final FileObject metaResource = metaResource(resource);
            if(metaResource == null) {
                return null;
            }
            final TermReader termReader = new TermReader(termFactory);
            final IStrategoTerm term = termReader.parseFromStream(metaResource.getContent().getInputStream());
            final String name = getSyntaxName(term.getSubterm(0));
            if(name == null) {
                return null;
            }
            final ILanguageImpl dialect = dialectService.getDialect(name);
            if(dialect == null) {
                final String message =
                    String.format("Resource %s requires dialect %s, but that dialect does not exist", resource, name);
                throw new MetaborgException(message);
            }
            final ILanguageImpl base;
            stratego2Active: {
                if(resource.getName().getExtension().equals("str2")) {
                    final ILanguage stratego2Language = languageService.getLanguage(SpoofaxConstants.LANG_STRATEGO2_NAME);
                    if(stratego2Language != null) {
                        final ILanguageImpl stratego2Impl = stratego2Language.activeImpl();
                        if(stratego2Impl != null) {
                            base = stratego2Language.activeImpl();
                            break stratego2Active;
                        }
                    }
                }
                base = dialectService.getBase(dialect);
            }
            return new IdentifiedDialect(dialect, base);
        } catch(ParseError | IOException e) {
            throw new MetaborgException("Unable to open or parse .meta file", e);
        }
    }

    @Override public boolean identify(FileObject resource, ILanguageImpl dialect) throws MetaborgException {
        final IdentifiedDialect identified = identify(resource);
        return dialect.equals(identified.dialect);
    }


    public static FileObject metaResource(FileObject resource) {
        try {
            final String path = resource.getName().getPath();
            final String fileName = FilenameUtils.getBaseName(path);
            if(fileName.isEmpty()) {
                return null;
            }
            final String metaResourceName = fileName + ".meta";
            final FileObject parent = resource.getParent();
            if(parent == null) {
                return null;
            }
            final FileObject metaResource = parent.getChild(metaResourceName);
            if(metaResource == null || !metaResource.exists()) {
                return null;
            }
            return metaResource;
        } catch(FileSystemException e) {
            return null;
        }
    }

    private static String getSyntaxName(IStrategoTerm entries) {
        for(IStrategoTerm entry : entries.getAllSubterms()) {
            final String cons = ((IStrategoAppl) entry).getConstructor().getName();
            if(cons.equals("Syntax")) {
                return TermUtils.toJavaStringAt(entry, 0);
            }
        }
        return null;
    }
}
