package org.metaborg.core.build.paths;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ResourceUtils;

import java.util.Collection;
import java.util.Set;

public class NewLanguagePathService implements INewLanguagePathService {
    private static final ILogger logger = LoggerUtils.logger(NewLanguagePathService.class);

    private final ILanguageIdentifierService languageIdentifierService;
    private final Set<INewLanguagePathProvider> providers;


    @Inject public NewLanguagePathService(ILanguageIdentifierService languageIdentifierService,
                                          Set<INewLanguagePathProvider> providers) {
        this.languageIdentifierService = languageIdentifierService;
        this.providers = providers;
    }


    @Override public Iterable<FileObject> sourcePaths(ILanguageSpec languageSpec, String languageName) {
        final Collection<Iterable<FileObject>> sources = Lists.newArrayList();
        for(INewLanguagePathProvider provider : providers) {
            try {
                sources.add(provider.sourcePaths(languageSpec, languageName));
            } catch(MetaborgException e) {
                logger.error("Getting source paths from provider {} failed unexpectedly, skipping this provider", e,
                    provider);
            }
        }
        return Iterables.concat(sources);
    }

    @Override public Iterable<FileObject> includePaths(ILanguageSpec languageSpec, String languageName) {
        final Collection<Iterable<FileObject>> includes = Lists.newArrayList();
        for(INewLanguagePathProvider provider : providers) {
            try {
                includes.add(provider.includePaths(languageSpec, languageName));
            } catch(MetaborgException e) {
                logger.error("Getting include paths from provider {} failed unexpectedly, skipping this provider", e,
                    provider);
            }
        }
        return Iterables.concat(includes);
    }

    @Override public Iterable<FileObject> sourceAndIncludePaths(ILanguageSpec languageSpec, String languageName) {
        return Iterables.concat(sourcePaths(languageSpec, languageName), includePaths(languageSpec, languageName));
    }


    @Override public Iterable<IdentifiedResource> sourceFiles(ILanguageSpec languageSpec, ILanguageImpl language) {
        final Iterable<FileObject> sourcePaths = sourcePaths(languageSpec, language.belongsTo().name());
        return toFiles(sourcePaths, language);
    }

    @Override public Iterable<IdentifiedResource> includeFiles(ILanguageSpec languageSpec, ILanguageImpl language) {
        final Iterable<FileObject> includePaths = includePaths(languageSpec, language.belongsTo().name());
        return toFiles(includePaths, language);
    }

    @Override public Iterable<IdentifiedResource> sourceAndIncludeFiles(ILanguageSpec languageSpec, ILanguageImpl language) {
        final Iterable<FileObject> paths = sourceAndIncludePaths(languageSpec, language.belongsTo().name());
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
