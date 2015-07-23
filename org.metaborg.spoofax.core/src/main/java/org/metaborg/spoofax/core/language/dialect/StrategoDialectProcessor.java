package org.metaborg.spoofax.core.language.dialect;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageChange;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.resource.ResourceChangeKind;
import org.metaborg.spoofax.core.SpoofaxProjectConstants;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.util.resource.ExtensionFileSelector;
import org.metaborg.util.resource.FileSelectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class StrategoDialectProcessor implements IDialectProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StrategoDialectProcessor.class);

    private final ILanguageService languageService;
    private final IDialectService dialectService;

    private final FileSelector selector;


    @Inject public StrategoDialectProcessor(ILanguageService languageService, IDialectService dialectService) {
        this.languageService = languageService;
        this.dialectService = dialectService;

        this.selector = FileSelectorUtils.and(new ExtensionFileSelector("tbl"), new SpoofaxIgnoresSelector());
    }


    @Override public void update(IProject project, Iterable<ResourceChange> changes) {
        final int numChanges = Iterables.size(changes);
        if(numChanges == 0) {
            return;
        }
        
        logger.debug("Processing dialect updates for " + project.location());

        final ILanguageImpl strategoLanguage = languageService.getLanguage(SpoofaxProjectConstants.LANG_STRATEGO_NAME);
        if(strategoLanguage == null) {
            logger.debug("Could not find Stratego language, Stratego dialects cannot be updated.");
            return;
        }
        final SyntaxFacet baseFacet = strategoLanguage.facets(SyntaxFacet.class);

        for(ResourceChange change : changes) {
            final FileObject resource = change.resource;
            try {
                if(!FileSelectorUtils.include(selector, resource, project.location())) {
                    continue;
                }
            } catch(FileSystemException e) {
                continue;
            }

            final String fileName = FilenameUtils.getBaseName(resource.getName().getBaseName());
            final SyntaxFacet newFacet =
                new SyntaxFacet(resource, baseFacet.startSymbols, baseFacet.singleLineCommentPrefixes,
                    baseFacet.multiLineCommentCharacters, baseFacet.fenceCharacters);
            final ResourceChangeKind changeKind = change.kind;
            try {
                switch(changeKind) {
                    case Create:
                        if(dialectService.hasDialect(fileName)) {
                            break;
                        }
                        dialectService.add(fileName, resource, strategoLanguage, newFacet);
                        break;
                    case Delete:
                        dialectService.remove(fileName);
                        break;
                    case Modify:
                        dialectService.update(fileName, newFacet);
                        break;
                    case Rename:
                        if(change.from != null) {
                            dialectService.remove(fileName);
                        }
                        if(change.to != null) {
                            if(dialectService.hasDialect(fileName)) {
                                break;
                            }
                            dialectService.add(fileName, resource, strategoLanguage, newFacet);
                        }
                        break;
                    case Copy:
                        if(change.to != null) {
                            if(dialectService.hasDialect(fileName)) {
                                break;
                            }
                            dialectService.add(fileName, resource, strategoLanguage, newFacet);
                        }
                        break;
                    default:
                        logger.error("Unhandled resource change kind {}", changeKind);
                        break;
                }
            } catch(MetaborgRuntimeException e) {
                logger.error("Failed to update dialect", e);
            }
        }
    }

    @Override public void update(LanguageChange change) {
        switch(change.kind) {
            case RELOAD:
            case RELOAD_ACTIVE:
            case REPLACE_ACTIVE:
                dialectService.update(change.oldLanguage, change.newLanguage);
                break;
            case REMOVE:
                dialectService.remove(change.oldLanguage);
                break;
            default:
                break;
        }
    }
}
