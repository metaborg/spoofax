package org.metaborg.spoofax.core.language.dialect;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.resource.ResourceChangeKind;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ExtensionFileSelector;
import org.metaborg.util.resource.FileSelectorUtils;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class DialectProcessor implements IDialectProcessor {
    private static final ILogger logger = LoggerUtils.logger(DialectProcessor.class);

    private final ILanguageService languageService;
    private final IDialectService dialectService;

    private final FileSelector selector;


    @Inject public DialectProcessor(ILanguageService languageService, IDialectService dialectService) {
        this.languageService = languageService;
        this.dialectService = dialectService;

        this.selector = FileSelectorUtils.and(new ExtensionFileSelector("tbl"), new SpoofaxIgnoresSelector());
    }


    @Override public void update(FileObject location, Iterable<ResourceChange> changes) {
        final int numChanges = Iterables.size(changes);
        if(numChanges == 0) {
            return;
        }

        final ILanguage strategoLanguage = languageService.getLanguage(SpoofaxConstants.LANG_STRATEGO_NAME);
        if(strategoLanguage == null) {
            logger.debug("Could not find Stratego language, Stratego dialects cannot be updated");
            return;
        }
        final ILanguageImpl strategoImpl = strategoLanguage.activeImpl();
        if(strategoImpl == null) {
            logger.debug(
                "Could not find active Stratego language implementation, " + "Stratego dialects cannot be updated");
            return;
        }

        logger.debug("Processing dialect updates for {}", location);

        // HACK: assuming single syntax facet
        final SyntaxFacet baseFacet = strategoImpl.facet(SyntaxFacet.class);
        if(baseFacet == null) {
            logger.debug("Active Stratego language implementation has no syntax facet, "
                + "Stratego dialects cannot be updated");
            return;
        }

        for(ResourceChange change : changes) {
            final FileObject resource = change.resource;
            try {
                if(!FileSelectorUtils.include(selector, resource, location)) {
                    continue;
                }
            } catch(FileSystemException e) {
                continue;
            }

            final String fileName = FilenameUtils.getBaseName(resource.getName().getBaseName());
            final SyntaxFacet newFacet = new SyntaxFacet(resource, baseFacet.startSymbols,
                baseFacet.singleLineCommentPrefixes, baseFacet.multiLineCommentCharacters, baseFacet.fenceCharacters);
            final ResourceChangeKind changeKind = change.kind;
            try {
                switch(changeKind) {
                    case Create:
                        add(fileName, resource, strategoImpl, newFacet);
                        break;
                    case Delete:
                        remove(fileName, resource);
                        break;
                    case Modify:
                        update(fileName, resource, newFacet);
                        break;
                    case Rename:
                        if(change.from != null) {
                            remove(fileName, resource);
                        }
                        if(change.to != null) {
                            add(fileName, resource, strategoImpl, newFacet);
                        }
                        break;
                    case Copy:
                        if(change.to != null) {
                            add(fileName, resource, strategoImpl, newFacet);
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

    @Override public void update(LanguageImplChange change) {
        switch(change.kind) {
            case Add:
                break;
            case Reload:
                dialectService.update(change.impl);
                break;
            case Remove:
                dialectService.remove(change.impl);
                break;
            default:
                break;
        }
    }


    private void add(String name, FileObject location, ILanguageImpl base, IFacet syntaxFacet) {
        if(dialectService.hasDialect(name)) {
            logger.debug("Trying to create dialect {} that already exists, from {}", name, location);
            return;
        }
        dialectService.add(name, location, base, syntaxFacet);
    }

    private void remove(String name, FileObject location) {
        if(!dialectService.hasDialect(name)) {
            logger.warn("Trying to delete dialect {} that does not exist, from {}", name, location);
            return;
        }
        dialectService.remove(name);
    }

    private void update(String name, FileObject location, IFacet syntaxFacet) {
        if(!dialectService.hasDialect(name)) {
            logger.warn("Trying to update dialect {} that does not exist, from {}", name, location);
            return;
        }
        dialectService.update(name, syntaxFacet);
    }
}
