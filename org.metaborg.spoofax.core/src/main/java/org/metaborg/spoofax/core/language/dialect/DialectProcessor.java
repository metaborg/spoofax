package org.metaborg.spoofax.core.language.dialect;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChangeKind;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoredDirectories;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.util.resource.ExtensionFileSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class DialectProcessor implements IDialectProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DialectProcessor.class);

    private final ILanguageService languageService;
    private final IDialectService dialectService;


    @Inject public DialectProcessor(ILanguageService languageService, IDialectService dialectService) {
        this.languageService = languageService;
        this.dialectService = dialectService;
    }


    @Override public void loadAll(FileObject directory) throws FileSystemException {
        final FileObject[] resources =
            directory.findFiles(SpoofaxIgnoredDirectories.ignoreFileSelector(new ExtensionFileSelector("tbl")));
        final ArrayList<IResourceChange> changes = Lists.newArrayListWithCapacity(resources.length);
        for(FileObject resource : resources) {
            changes.add(new ResourceChange(resource));
        }
        update(changes);
    }

    @Override public void removeAll(FileObject directory) throws FileSystemException {
        final FileObject[] resources =
            directory.findFiles(SpoofaxIgnoredDirectories.ignoreFileSelector(new ExtensionFileSelector("tbl")));
        final ArrayList<IResourceChange> changes = Lists.newArrayListWithCapacity(resources.length);
        for(FileObject resource : resources) {
            changes.add(new ResourceChange(resource, ResourceChangeKind.Delete));
        }
        update(changes);
    }

    @Override public void update(Iterable<IResourceChange> changes) {
        final ILanguage strategoLanguage = languageService.get("Stratego-Sugar");
        if(strategoLanguage == null) {
            logger.debug("Could not find Stratego language, Stratego dialects cannot be updated.");
            return;
        }
        final SyntaxFacet baseFacet = strategoLanguage.facet(SyntaxFacet.class);

        logger.debug("Updating {} Stratego dialects", Iterables.size(changes));
        for(IResourceChange change : changes) {
            final FileObject resource = change.resource();
            final String name = FilenameUtils.getBaseName(resource.getName().getBaseName());
            final SyntaxFacet newFacet = new SyntaxFacet(resource, Sets.newHashSet(baseFacet.startSymbols()));
            final ResourceChangeKind changeKind = change.kind();
            try {
                switch(changeKind) {
                    case Create:
                        if(dialectService.hasDialect(name)) {
                            break;
                        }
                        dialectService.add(name, resource, strategoLanguage, newFacet);
                        break;
                    case Delete:
                        dialectService.remove(name);
                        break;
                    case Modify:
                        dialectService.update(name, newFacet);
                        break;
                    case Rename:
                        if(change.from() != null) {
                            dialectService.remove(name);
                        }
                        if(change.to() != null) {
                            if(dialectService.hasDialect(name)) {
                                break;
                            }
                            dialectService.add(name, resource, strategoLanguage, newFacet);
                        }
                        break;
                    case Copy:
                        if(change.to() != null) {
                            if(dialectService.hasDialect(name)) {
                                break;
                            }
                            dialectService.add(name, resource, strategoLanguage, newFacet);
                        }
                        break;
                    default:
                        logger.error("Unhandled resource change kind {}", changeKind);
                        break;
                }
            } catch(SpoofaxRuntimeException e) {
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
