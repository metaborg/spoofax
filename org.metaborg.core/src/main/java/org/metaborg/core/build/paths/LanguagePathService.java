package org.metaborg.core.build.paths;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ResourceUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LanguagePathService implements ILanguagePathService {
    private static final ILogger logger = LoggerUtils.logger(LanguagePathService.class);

    private final ILanguageIdentifierService languageIdentifierService;
    private final Set<ILanguagePathProvider> providers;


    @Inject public LanguagePathService(ILanguageIdentifierService languageIdentifierService,
        Set<ILanguagePathProvider> providers) {
        this.languageIdentifierService = languageIdentifierService;
        this.providers = providers;
    }


    @Override public Iterable<FileObject> sourcePaths(IProject project, String languageName) {
        final Collection<Iterable<FileObject>> sources = Lists.newArrayList();
        for(ILanguagePathProvider provider : providers) {
            try {
                sources.add(provider.sourcePaths(project, languageName));
            } catch(MetaborgException e) {
                logger.error("Getting source paths from provider {} failed unexpectedly, skipping this provider", e,
                    provider);
            }
        }
        return Iterables.concat(sources);
    }

    @Override public Iterable<FileObject> includePaths(IProject project, String languageName) {
        final Collection<Iterable<FileObject>> includes = Lists.newArrayList();
        for(ILanguagePathProvider provider : providers) {
            try {
                includes.add(provider.includePaths(project, languageName));
            } catch(MetaborgException e) {
                logger.error("Getting include paths from provider {} failed unexpectedly, skipping this provider", e,
                    provider);
            }
        }
        return Iterables.concat(includes);
    }

    @Override public Iterable<FileObject> sourceAndIncludePaths(IProject project, String languageName) {
        return Iterables.concat(sourcePaths(project, languageName), includePaths(project, languageName));
    }


    @Override public Iterable<IdentifiedResource> sourceFiles(IProject project, ILanguageImpl language) {
        final Iterable<FileObject> sourcePaths = sourcePaths(project, language.belongsTo().name());
        return toFiles(sourcePaths, language);
    }

    @Override public Iterable<IdentifiedResource> includeFiles(IProject project, ILanguageImpl language) {
        final Iterable<FileObject> includePaths = includePaths(project, language.belongsTo().name());
        return toFiles(includePaths, language);
    }

    @Override public Iterable<IdentifiedResource> sourceAndIncludeFiles(IProject project, ILanguageImpl language) {
        final Iterable<FileObject> paths = sourceAndIncludePaths(project, language.belongsTo().name());
        return toFiles(paths, language);
    }


    @Override public Iterable<IdentifiedResource> toFiles(Iterable<FileObject> paths, ILanguageImpl language) {
        final Iterable<FileObject> files = ResourceUtils.expand(paths);
        final Collection<IdentifiedResource> identifiedFiles = Lists.newArrayList();
        for(FileObject file : files) {
            final IdentifiedResource identifiedFile =
                languageIdentifierService.identifyToResource(file, Iterables2.singleton(language));
            if(identifiedFile != null) {
                identifiedFiles.add(identifiedFile);
            }
        }
        return identifiedFiles;
    }
}
